package com.example.repartidor.viewmodel

import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.DetalleVentaUI
import com.example.repartidor.data.model.VentaUI
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.data.repository.VentasDiaRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketBuilder
import com.example.repartidor.utils.TicketItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VentasDiaViewModel(
    private val repository: VentasDiaRepository,
    private val printerRepository: PrinterRepository,
    private val printerManager: PrinterManager,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val sessionManager: SessionManager
) : ViewModel() {

    var ventas by mutableStateOf<List<VentaUI>>(emptyList())
        private set

    var detalleVenta by mutableStateOf<List<DetalleVentaUI>>(emptyList())
        private set

    var ventaSeleccionada by mutableStateOf<VentaUI?>(null)
        private set

    var mostrarDialogo by mutableStateOf(false)
        private set

    var totalAbonos by mutableStateOf(0.0)
        private set


    fun cargarVentas() {
        viewModelScope.launch {
            val usuarioId = sessionManager.getUserId()
                ?: return@launch

            val data = repository.getVentasDelDia(usuarioId)

            ventas = data
        }
    }


    fun seleccionarVenta(venta: VentaUI) {
        viewModelScope.launch {

            ventaSeleccionada = venta

            val (detalle, abonos) = repository.getDetalleVenta(venta.id)

            detalleVenta = detalle
            totalAbonos = abonos

            mostrarDialogo = true
        }
    }


    fun cerrarDialogo() {
        mostrarDialogo = false
        ventaSeleccionada = null
        detalleVenta = emptyList()
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun imprimirVentaSeleccionada(
        onResult: (PrintResult) -> Unit
    ) {
        viewModelScope.launch {

            val venta = ventaSeleccionada ?: return@launch

            try {

                val ticketItems = detalleVenta.map {
                    TicketItem(
                        nombre = it.nombreCompleto,
                        presentacion = "", // si no tienes separado no pasa nada
                        cantidad = it.cantidad.toInt(),
                        precioUnitario = it.precioUnitario
                    )
                }

                val subtotal = detalleVenta.sumOf { it.subtotal }
                val total = venta.total
                val descuento = subtotal - total

                val ticket = TicketBuilder.build(
                    items = ticketItems,
                    clienteNombre = venta.nombreCliente,
                    clienteNegocio = venta.nombreNegocio,
                    subtotal = subtotal,
                    porcentajeDescuento = 0.0, // si no lo tienes guardado
                    descuento = descuento,
                    totalFinal = total,
                    fecha = venta.fecha,
                    tipoVenta = venta.tipoVenta,
                    abonoInicial = totalAbonos
                )

                val device = bluetoothAdapter?.let {
                    printerRepository.getSavedPrinter(it)
                }

                val result = withContext(Dispatchers.IO) {
                    printerManager.print(device, ticket)
                }

                onResult(result)

            } catch (e: Exception) {
                onResult(PrintResult.Error(e.message ?: "Error al imprimir"))
            }
        }
    }
}