package com.example.repartidor.viewmodel.Devoluciones

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.dclass.SustitucionSeleccionada
import com.example.repartidor.data.model.entity.DevolucionEntity
import com.example.repartidor.data.repository.DevolucionInventarioRepository
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketDevolucionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class DevolucionViewModel(
    private val repository: DevolucionInventarioRepository,
    private val sessionManager: SessionManager,
    private val printerRepository: PrinterRepository,
    private val printerManager: PrinterManager,
    private val bluetoothAdapter: BluetoothAdapter?
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var success by mutableStateOf(false)
        private set

    var printResult by mutableStateOf<PrintResult?>(null)
        private set

    var devolucionRegistrada by mutableStateOf<DevolucionEntity?>(null)
        private set

    private var devolucionYaRegistrada = false

    fun getVariacionesDisponiblesParaSustitucion(): Flow<List<ProductoSustitucion>> = flow {
        val miniBodegaId = sessionManager.getMiniBodegaId()
        if (miniBodegaId == null) {
            emit(emptyList())
            return@flow
        }
        emitAll(repository.getVariacionesDisponiblesParaSustitucion(miniBodegaId))
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun registrarDevolucion(
        clienteId: Int?,
        clienteNombre: String?,
        clienteNulo: Boolean,
        motivo: String,
        observacion: String,
        carrito: List<CarritoItem>,
        sustitucionesPorProducto: Map<Int, List<SustitucionSeleccionada>>,
        imprimir: Boolean
    ) {
        if (isLoading || devolucionYaRegistrada) return

        isLoading = true
        error = null
        success = false
        printResult = null
        devolucionRegistrada = null

        viewModelScope.launch {
            try {
                val miniBodegaId = sessionManager.getMiniBodegaId()
                val usuarioId = sessionManager.getUserId()
                val usuarioNombre = sessionManager.getUser()

                if (usuarioId == null || miniBodegaId == null) {
                    error = "Sesión inválida"
                    return@launch
                }
                if (carrito.isEmpty()) {
                    error = "No hay productos en devolución"
                    return@launch
                }
                if (motivo.isBlank()) {
                    error = "Debes seleccionar un motivo"
                    return@launch
                }

                val fechaActual = System.currentTimeMillis()
                val devolucion = DevolucionEntity(
                    uuid = UUID.randomUUID().toString(),
                    tipo = motivo,
                    clienteId = if (clienteNulo) null else clienteId,
                    usuarioId = usuarioId,
                    miniBodegaId = miniBodegaId,
                    fecha = fechaActual.toString(),
                    descripcion = observacion,
                    createdAt = fechaActual.toString(),
                    sincronizado = false
                )

                // REGISTRAR DEVOLUCIÓN E INVENTARIO
                val registrada = repository.registrarDevolucion(
                    devolucion = devolucion,
                    carrito = carrito,
                    sustitucionesPorProducto = sustitucionesPorProducto
                )

                // DESDE AQUÍ LA DEVOLUCIÓN YA ESTÁ GUARDADA
                devolucionRegistrada = registrada
                devolucionYaRegistrada = true

                // LA IMPRESIÓN NO DEBE AFECTAR EL REGISTRO
                if (imprimir) {
                    printResult = try {
                        val ticket = TicketDevolucionBuilder.build(
                            items = carrito,
                            sustitucionesPorProducto = sustitucionesPorProducto,
                            clienteNombre = when {
                                clienteNulo -> "Venta rápida"
                                clienteNombre != null -> clienteNombre
                                else -> null
                            },
                            motivo = motivo,
                            observacion = observacion,
                            usuario = usuarioNombre,
                            fecha = fechaActual,
                            diferenciaDinero = registrada.diferenciaDinero,
                            tipoDiferencia = registrada.tipoDiferencia,
                            formaLiquidacion = registrada.formaLiquidacion
                        )

                        withContext(Dispatchers.IO) {
                            val device = bluetoothAdapter?.let {
                                printerRepository.getSavedPrinter(it)
                            }
                            val firstPrint = printerManager.print(device, ticket)
                            if (firstPrint is PrintResult.Success) {
                                printerManager.print(device, ticket)
                            } else {
                                firstPrint
                            }
                        }
                    } catch (e: Exception) {
                        PrintResult.Error(e.message ?: "Error al imprimir ticket")
                    }
                } else {
                    printResult = PrintResult.NoPrinter
                }
                success=true

            } catch (e: Exception) {
                error = e.message ?: "Error al registrar devolución"
            } finally {
                isLoading = false
            }
        }
    }

    fun reset() {
        // Solo reiniciar cuando ya no exista una operación en curso
        if (isLoading) return
        error = null
        success = false
        printResult = null
        devolucionRegistrada = null
        devolucionYaRegistrada = false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun verificarImpresora(): PrintResult {
        val device = bluetoothAdapter?.let {
            printerRepository.getSavedPrinter(it)
        }
        return printerManager.checkPrinter(device)
    }
}