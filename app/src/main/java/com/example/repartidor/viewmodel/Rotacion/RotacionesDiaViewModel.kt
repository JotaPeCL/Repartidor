package com.example.repartidor.viewmodel.Rotacion

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.local.PresentacionProductoTerminadoDao
import com.example.repartidor.data.local.ProductoVariacionDao
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.SustitucionRotacionSeleccionada
import com.example.repartidor.data.model.entity.RotacionDetalleEntity
import com.example.repartidor.data.model.entity.RotacionEntity
import com.example.repartidor.data.model.entity.RotacionSustitucionEntity
import com.example.repartidor.data.repository.CancelacionRotacionRepository
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.data.repository.RotacionesDiaRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketRotacionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class RotacionesDiaViewModel(
    private val repository: RotacionesDiaRepository,
    private val cancelacionRepository: CancelacionRotacionRepository,
    private val sessionManager: SessionManager,
    private val clienteDao: ClienteDao,
    private val productoVariacionDao: ProductoVariacionDao,
    private val presentacionDao: PresentacionProductoTerminadoDao,
    private val printerRepository: PrinterRepository,
    private val printerManager: PrinterManager,
    private val bluetoothAdapter: BluetoothAdapter?
) : ViewModel() {

    private val _rotaciones =
        MutableStateFlow<List<RotacionEntity>>(emptyList())
    val rotaciones: StateFlow<List<RotacionEntity>> = _rotaciones

    private val _rotacionSeleccionada =
        MutableStateFlow<RotacionEntity?>(null)
    val rotacionSeleccionada: StateFlow<RotacionEntity?> =
        _rotacionSeleccionada

    private val _productosRecibidos =
        MutableStateFlow<List<RotacionDetalleEntity>>(emptyList())
    val productosRecibidos: StateFlow<List<RotacionDetalleEntity>> =
        _productosRecibidos

    private val _productosEntregados =
        MutableStateFlow<List<RotacionSustitucionEntity>>(emptyList())
    val productosEntregados: StateFlow<List<RotacionSustitucionEntity>> =
        _productosEntregados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success

    private val _printResult = MutableStateFlow<PrintResult?>(null)
    val printResult: StateFlow<PrintResult?> = _printResult

    // =========================================================
    // CARGAR ROTACIONES DEL DÍA
    // =========================================================

    fun cargarRotaciones() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val usuarioId = sessionManager.getUserId()
                    ?: throw Exception("No se encontró el usuario")

                _rotaciones.value =
                    repository.getRotacionesDelDia(usuarioId)

            } catch (e: Exception) {
                _error.value =
                    e.message ?: "Error al cargar las rotaciones"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================
    // SELECCIONAR ROTACIÓN
    // =========================================================

    fun seleccionarRotacion(rotacionId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val rotacion = repository.getRotacionById(rotacionId)
                    ?: throw Exception("Rotación no encontrada")

                val usuarioId = sessionManager.getUserId()
                    ?: throw Exception("No se encontró el usuario")

                val miniBodegaId = sessionManager.getMiniBodegaId()
                    ?: throw Exception("No se encontró la MiniBodega")

                if (
                    rotacion.usuarioId != usuarioId ||
                    rotacion.miniBodegaId != miniBodegaId
                ) {
                    throw Exception("No puedes consultar esta rotación")
                }

                val recibidos =
                    repository.getProductosRecibidos(rotacionId)

                val entregados =
                    repository.getProductosEntregados(rotacionId)

                _rotacionSeleccionada.value = rotacion
                _productosRecibidos.value = recibidos
                _productosEntregados.value = entregados

            } catch (e: Exception) {
                _rotacionSeleccionada.value = null
                _productosRecibidos.value = emptyList()
                _productosEntregados.value = emptyList()

                _error.value =
                    e.message ?: "Error al consultar la rotación"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================
    // CANCELAR ROTACIÓN
    // =========================================================

    fun cancelarRotacion(
        rotacionId: Int,
        motivo: String
    ) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                val usuarioId = sessionManager.getUserId()
                    ?: throw Exception("No se encontró el usuario")

                val miniBodegaId = sessionManager.getMiniBodegaId()
                    ?: throw Exception("No se encontró la MiniBodega")

                cancelacionRepository.cancelarRotacion(
                    rotacionId = rotacionId,
                    usuarioId = usuarioId,
                    miniBodegaId = miniBodegaId,
                    motivo = motivo
                )

                _rotacionSeleccionada.value = null
                _productosRecibidos.value = emptyList()
                _productosEntregados.value = emptyList()

                _rotaciones.value =
                    repository.getRotacionesDelDia(usuarioId)

                _success.value = "Rotación cancelada correctamente"

            } catch (e: Exception) {
                _error.value =
                    e.message ?: "Error al cancelar la rotación"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================
    // REIMPRIMIR TICKET
    // =========================================================

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun reimprimirTicket(rotacionId: Int) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _printResult.value = null

            try {
                val usuarioId = sessionManager.getUserId()
                    ?: throw Exception("No se encontró el usuario")

                val miniBodegaId = sessionManager.getMiniBodegaId()
                    ?: throw Exception("No se encontró la MiniBodega")

                val rotacion = repository.getRotacionById(rotacionId)
                    ?: throw Exception("Rotación no encontrada")

                if (
                    rotacion.usuarioId != usuarioId ||
                    rotacion.miniBodegaId != miniBodegaId
                ) {
                    throw Exception("No puedes reimprimir esta rotación")
                }

                val recibidos =
                    repository.getProductosRecibidos(rotacionId)

                val entregados =
                    repository.getProductosEntregados(rotacionId)

                if (recibidos.isEmpty()) {
                    throw Exception("La rotación no tiene productos")
                }

                // Reconstruir los productos recibidos

                val carrito = recibidos.map { detalle ->

                    val presentacion =
                        obtenerPresentacion(
                            detalle.productoVariacionId
                        )

                    CarritoItem(
                        productoVariacionId =
                            detalle.productoVariacionId,
                        productoNombre =
                            detalle.nombreProducto,
                        presentacionNombre = presentacion,
                        precio = detalle.precioUnitario,
                        cantidad = convertirCantidad(detalle.cantidad)
                    )
                }

                // Reconstruir las sustituciones asociadas
                // a cada detalle recibido

                val sustitucionesPorProducto =
                    mutableMapOf<
                            Int,
                            MutableList<SustitucionRotacionSeleccionada>
                            >()

                for (detalle in recibidos) {

                    val cambios = entregados.filter {
                        it.rotacionDetalleId == detalle.id
                    }

                    val lista = cambios.map { cambio ->

                        val presentacion =
                            obtenerPresentacion(
                                cambio.productoVariacionId
                            )

                        SustitucionRotacionSeleccionada(
                            productoVariacionId =
                                cambio.productoVariacionId,
                            nombreProducto =
                                cambio.nombreProducto,
                            presentacionNombre = presentacion,
                            precio = cambio.precioUnitario,
                            cantidad = convertirCantidad(cambio.cantidad)
                        )
                    }

                    sustitucionesPorProducto
                        .getOrPut(detalle.productoVariacionId) {
                            mutableListOf()
                        }
                        .addAll(lista)
                }

                val clienteNombre = rotacion.clienteId?.let {
                    clienteDao.getClienteById(it)?.nombre
                }

                val ticket = TicketRotacionBuilder.build(
                    items = carrito,
                    sustitucionesPorProducto = sustitucionesPorProducto,
                    clienteNombre = clienteNombre,
                    descripcion = rotacion.descripcion,
                    usuario = sessionManager.getUser(),
                    fecha = rotacion.fecha.toLong()
                )

                _printResult.value = withContext(Dispatchers.IO) {

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
                _error.value =
                    e.message ?: "Error al reimprimir el ticket"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================
    // RECUPERAR PRESENTACIÓN
    // =========================================================

    private suspend fun obtenerPresentacion(
        productoVariacionId: Int
    ): String {

        val variacion =
            productoVariacionDao.getById(productoVariacionId)
                ?: return "Sin presentación"

        val presentacion =
            presentacionDao.getById(variacion.presentacion)

        return presentacion?.nombre ?: "Sin presentación"
    }

    // =========================================================
    // CONVERTIR CANTIDAD
    // =========================================================

    private fun convertirCantidad(cantidad: Double): Int {

        if (!cantidad.isFinite() ||
            cantidad < 0 ||
            cantidad > Int.MAX_VALUE ||
            abs(cantidad - cantidad.toInt()) > 0.000001
        ) {
            throw Exception(
                "La cantidad $cantidad no se puede representar en el ticket"
            )
        }

        return cantidad.toInt()
    }

    // =========================================================
    // LIMPIAR ESTADOS
    // =========================================================

    fun limpiarEstado() {
        _error.value = null
        _success.value = null
        _printResult.value = null
    }

    fun limpiarSeleccion() {
        _rotacionSeleccionada.value = null
        _productosRecibidos.value = emptyList()
        _productosEntregados.value = emptyList()
    }
}