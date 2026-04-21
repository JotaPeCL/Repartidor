package com.example.repartidor.viewmodel

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.data.repository.VentaLocalRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketBuilder
import com.example.repartidor.utils.TicketItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VentaProcesoViewModel(
    private val repository: VentaLocalRepository,
    private val sessionManager: SessionManager,
    private val printerRepository: PrinterRepository,
    private val printerManager: PrinterManager,
    private val bluetoothAdapter: BluetoothAdapter?
) : ViewModel() {

    var clienteId: Int? = null
        private set

    fun setCliente(id: Int?) {
        clienteId = id
    }

    fun reset() {
        clienteId = null
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun confirmarVenta(
        items: List<CarritoItem>,
        onSuccess: (PrintResult) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {

                val snapshot = items.map { it.copy() }

                val usuarioId = sessionManager.getUserId()
                    ?: return@launch onError("Usuario no identificado")

                val miniBodegaId = sessionManager.getMiniBodegaId()
                    ?: return@launch onError("No hay mini bodega activa")

                repository.guardarVenta(
                    clienteId = clienteId,
                    usuarioId = usuarioId,
                    items = snapshot,
                    miniBodegaId = miniBodegaId
                )

                val ticketItems = snapshot.map {
                    TicketItem(
                        nombre = it.productoNombre,
                        presentacion = it.presentacionNombre,
                        cantidad = it.cantidad,
                        precioUnitario = it.precio
                    )
                }

                val cliente = clienteId?.let {
                    repository.getClienteById(it)
                }

                val ticket = TicketBuilder.build(
                    items = ticketItems,
                    clienteNombre = cliente?.nombre,
                    clienteNegocio = cliente?.nombreNegocio
                )

                val device = bluetoothAdapter?.let {
                    printerRepository.getSavedPrinter(it)
                }
                delay(300)

                val result = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    printerManager.print(device, ticket)
                }

                onSuccess(result)

            } catch (e: Exception) {
                onError(e.message ?: "Error al vender")
            }
        }
    }
}