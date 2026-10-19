package com.example.repartidor.viewmodel.Rotacion

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.ProductoConStock
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.dclass.SustitucionRotacionSeleccionada
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import com.example.repartidor.data.model.entity.RotacionEntity
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.data.repository.RotacionInventarioRepository
import com.example.repartidor.data.repository.RotacionRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketRotacionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToLong

class RotacionViewModel(
    private val repository: RotacionRepository,
    private val inventarioRepository: RotacionInventarioRepository,
    private val sessionManager: SessionManager,
    private val printerRepository: PrinterRepository,
    private val printerManager: PrinterManager,
    private val bluetoothAdapter: BluetoothAdapter?
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private var rotacionYaRegistrada = false

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _productos =
        MutableStateFlow<List<ProductoTerminadoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoTerminadoEntity>> = _productos

    private val _variaciones =
        MutableStateFlow<List<ProductoConStock>>(emptyList())
    val variaciones: StateFlow<List<ProductoConStock>> = _variaciones

    private val _variacionesSustitucion =
        MutableStateFlow<List<ProductoSustitucion>>(emptyList())

    private val _printResult = MutableStateFlow<PrintResult?>(null)
    val printResult: StateFlow<PrintResult?> = _printResult

    val variacionesSustitucion: StateFlow<List<ProductoSustitucion>> =
        _variacionesSustitucion

    fun cargarProductos() {
        viewModelScope.launch {
            val miniBodegaId =
                sessionManager.getMiniBodegaId()
                    ?: return@launch

            repository.getProductos(miniBodegaId)
                .collect {
                    _productos.value = it
                }
        }
    }

    fun cargarVariaciones(productoId: Int) {
        viewModelScope.launch {
            val miniBodegaId =
                sessionManager.getMiniBodegaId()
                    ?: return@launch

            repository.getVariaciones(
                productoId = productoId,
                miniBodegaId = miniBodegaId
            ).collect {
                _variaciones.value = it
            }
        }
    }

    fun cargarVariacionesParaSustitucion() {
        viewModelScope.launch {
            val miniBodegaId =
                sessionManager.getMiniBodegaId()
                    ?: return@launch

            repository.getVariacionesParaSustitucion(
                miniBodegaId = miniBodegaId
            ).collect {
                _variacionesSustitucion.value = it
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun registrarRotacion(
        clienteId: Int?,
        carrito: List<CarritoItem>,
        sustitucionesPorProducto: Map<Int, List<SustitucionRotacionSeleccionada>>,
        descripcion: String,
        clienteNombre: String? = null,
        imprimir: Boolean = true
    ) {
            if (_isLoading.value || rotacionYaRegistrada) return
            _isLoading.value = true

            viewModelScope.launch {
                _error.value = null
                _success.value = false
                _printResult.value = null

            try {
                if (carrito.isEmpty()) {
                    throw Exception(
                        "Debes seleccionar al menos un producto para rotar"
                    )
                }

                val usuarioId = sessionManager.getUserId()
                    ?: throw Exception(
                        "No se encontró el usuario de la sesión"
                    )

                val miniBodegaId = sessionManager.getMiniBodegaId()
                    ?: throw Exception(
                        "No se encontró la MiniBodega de la sesión"
                    )

                // VALIDAR PRODUCTOS DE CAMBIO

                carrito.forEach { item ->
                    val sustituciones =
                        sustitucionesPorProducto[item.productoVariacionId]
                            ?: emptyList()

                    if (sustituciones.isEmpty()) {
                        throw Exception(
                            "Debes seleccionar productos de cambio para ${item.productoNombre}"
                        )
                    }
                }

                // CALCULAR VALOR RECIBIDO

                val valorRecibidoCentavos = carrito.sumOf { item ->
                    (item.precio * item.cantidad * 100.0).roundToLong()
                }

                // CALCULAR VALOR ENTREGADO

                val valorEntregadoCentavos = carrito.sumOf { item ->
                    val sustituciones =
                        sustitucionesPorProducto[item.productoVariacionId]
                            ?: emptyList()

                    sustituciones.sumOf { sustitucion ->
                        (
                                sustitucion.precio *
                                        sustitucion.cantidad *
                                        100.0
                                ).roundToLong()
                    }
                }

                // CALCULAR DIFERENCIA

                val diferenciaCentavos =
                    valorEntregadoCentavos - valorRecibidoCentavos

                val tipoDiferencia = when {
                    diferenciaCentavos > 0 -> "CLIENTE_PAGO"
                    diferenciaCentavos < 0 -> "REPARTIDOR_PAGO"
                    else -> "SIN_DIFERENCIA"
                }

                val diferenciaDinero =
                    abs(diferenciaCentavos) / 100.0

                // CREAR ROTACIÓN

                val fecha = System.currentTimeMillis().toString()

                val rotacion = RotacionEntity(
                    uuid = UUID.randomUUID().toString(),
                    clienteId = clienteId,
                    usuarioId = usuarioId,
                    miniBodegaId = miniBodegaId,
                    fecha = fecha,
                    descripcion = descripcion,
                    diferenciaDinero = diferenciaDinero,
                    tipoDiferencia = tipoDiferencia,
                    formaLiquidacion = "EFECTIVO",
                    sincronizado = false,
                    createdAt = fecha
                )

                // REGISTRAR ROTACIÓN E INVENTARIO

                inventarioRepository.registrarRotacion(
                    rotacion = rotacion,
                    carrito = carrito,
                    sustitucionesPorProducto = sustitucionesPorProducto
                )

                // Desde aquí la rotación ya quedó registrada.
                // Una falla de impresión no debe cambiar ese resultado.
                rotacionYaRegistrada = true
                _success.value = true

                if (imprimir) {
                    _printResult.value = try {

                        val ticket = TicketRotacionBuilder.build(
                            items = carrito,
                            sustitucionesPorProducto = sustitucionesPorProducto,
                            clienteNombre = clienteNombre,
                            descripcion = descripcion,
                            usuario = sessionManager.getUser(),
                            fecha = fecha.toLong()
                        )

                        withContext(Dispatchers.IO) {
                            val device = bluetoothAdapter?.let {
                                printerRepository.getSavedPrinter(it)
                            }

                            val firstPrint =
                                printerManager.print(device, ticket)

                            if (firstPrint is PrintResult.Success) {
                                printerManager.print(device, ticket)
                            } else {
                                firstPrint
                            }
                        }

                    } catch (e: Exception) {
                        PrintResult.Error(
                            e.message ?: "Error al imprimir ticket"
                        )
                    }
                } else {
                    _printResult.value = PrintResult.NoPrinter
                }

            } catch (e: Exception) {
                _error.value =
                    e.message ?: "Error al registrar la rotación"

            } finally {
                _isLoading.value = false
            }
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun verificarImpresora(): PrintResult {

        val device = bluetoothAdapter?.let {
            printerRepository.getSavedPrinter(it)
        }

        return printerManager.checkPrinter(device)
    }


    fun limpiarEstado() {
        _error.value = null
        _success.value = false
    }
}