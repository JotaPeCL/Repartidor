package com.example.repartidor.viewmodel

import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.model.DevolucionEntity
import com.example.repartidor.data.repository.DevolucionInventarioRepository
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketDevolucionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

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

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun registrarDevolucion(
        clienteId: Int?,
        clienteNulo: Boolean,
        motivo: String,
        observacion: String,
        carrito: List<CarritoItem>,
        imprimir: Boolean,
    ) {

        isLoading = true
        error = null
        success = false
        printResult = null

        viewModelScope.launch {
            try {

                val miniBodegaId = sessionManager.getMiniBodegaId()
                val usuarioId = sessionManager.getUserId()

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

                val devolucion = DevolucionEntity(
                    tipo = motivo,
                    clienteId = if (clienteNulo) null else clienteId,
                    usuarioId = usuarioId,
                    miniBodegaId = miniBodegaId,
                    fecha = System.currentTimeMillis().toString(),
                    descripcion = observacion,
                    createdAt = System.currentTimeMillis().toString(),
                    sincronizado = false
                )

                repository.registrarDevolucion(
                    devolucion = devolucion,
                    carrito = carrito
                )
                // ─────────────────────────────
                // 2. GENERAR TICKET
                // ─────────────────────────────
                val ticket = TicketDevolucionBuilder.build(
                    items = carrito,
                    clienteNombre = clienteId?.toString(),
                    motivo = motivo,
                    observacion = observacion,
                    usuario = usuarioId.toString(),
                    fecha = System.currentTimeMillis()
                )

                val device = bluetoothAdapter?.let {
                    printerRepository.getSavedPrinter(it)
                }

                // ─────────────────────────────
                // 🔥 IMPRESIÓN CONTROLADA
                // ─────────────────────────────
                printResult = if (imprimir) {
                    withContext(Dispatchers.IO) {
                        printerManager.print(device, ticket)
                    }
                } else {
                    PrintResult.NoPrinter
                }

                success = true

            } catch (e: Exception) {
                error = e.message ?: "Error al registrar devolución"
            } finally {
                isLoading = false
            }
        }
    }

    fun reset() {
        error = null
        success = false
        isLoading = false
    }

    fun setLoading(value: Boolean) {
        isLoading = value
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun verificarImpresora(): PrintResult {

        val device = bluetoothAdapter?.let {
            printerRepository.getSavedPrinter(it)
        }

        return printerManager.checkPrinter(device)
    }
}
