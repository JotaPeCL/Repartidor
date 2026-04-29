package com.example.repartidor.viewmodel

import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.data.repository.VentaLocalRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketBuilder
import com.example.repartidor.utils.TicketItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val _cliente = MutableStateFlow<ClienteEntity?>(null)
    val cliente: StateFlow<ClienteEntity?> = _cliente


    fun setCliente(id: Int?) {
        clienteId = id
        viewModelScope.launch {
            _cliente.value = id?.let { repository.getClienteById(it) }
        }
    }

    fun reset() {
        clienteId = null
        _cliente.value = null
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun confirmarVenta(
        items: List<CarritoItem>,
        imprimir: Boolean, // 🔥 NUEVO (único cambio en firma)
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

                val cliente = clienteId?.let {
                    repository.getClienteById(it)
                }

                val subtotal = snapshot.sumOf { it.precio * it.cantidad }
                val porcentaje = cliente?.porcentajeDescuento ?: 0.0
                val descuento = subtotal * (porcentaje / 100.0)
                val totalFinal = subtotal - descuento

                // 🔥 GUARDAR VENTA (NO SE TOCA)
                repository.guardarVenta(
                    clienteId = clienteId,
                    usuarioId = usuarioId,
                    items = snapshot,
                    miniBodegaId = miniBodegaId,
                    total = totalFinal
                )

                val ticketItems = snapshot.map {
                    TicketItem(
                        nombre = it.productoNombre,
                        presentacion = it.presentacionNombre,
                        cantidad = it.cantidad,
                        precioUnitario = it.precio
                    )
                }

                val ticket = TicketBuilder.build(
                    items = ticketItems,
                    clienteNombre = cliente?.nombre,
                    clienteNegocio = cliente?.nombreNegocio,
                    subtotal = subtotal,
                    porcentajeDescuento = porcentaje,
                    descuento = descuento,
                    totalFinal = totalFinal
                )

                val device = bluetoothAdapter?.let {
                    printerRepository.getSavedPrinter(it)
                }

                delay(300)

                // 🔥 AQUÍ ESTÁ EL ÚNICO CAMBIO REAL
                val result = if (imprimir) {
                    withContext(kotlinx.coroutines.Dispatchers.IO) {
                        printerManager.print(device, ticket)
                    }
                } else {
                    PrintResult.NoPrinter
                }

                onSuccess(result)

            } catch (e: Exception) {
                onError(e.message ?: "Error al vender")
            }
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun verificarImpresora(): PrintResult {

        val device = bluetoothAdapter?.let {
            printerRepository.getSavedPrinter(it)
        }

        return printerManager.checkPrinter(device)
    }
}