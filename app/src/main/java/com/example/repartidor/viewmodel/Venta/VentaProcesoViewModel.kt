package com.example.repartidor.viewmodel.Venta

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.entity.ClienteEntity
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.data.repository.VentaLocalRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketBuilder
import com.example.repartidor.utils.TicketItem
import kotlinx.coroutines.Dispatchers
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun confirmarVenta(
        items: List<CarritoItem>,
        tipoVenta: String,
        abonoInicial: Double,
        imprimir: Boolean,
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

                if (tipoVenta == "CREDITO" && cliente != null) {

                    val creditoDisponible =
                        cliente.limiteCredito - cliente.saldoAdeudo

                    if (creditoDisponible <= 0) {
                        return@launch onError("El cliente no tiene crédito disponible")
                    }

                    if (totalFinal > creditoDisponible) {
                        return@launch onError("La venta excede el crédito disponible")
                    }
                }

                if (tipoVenta == "CREDITO" && cliente == null) {
                    return@launch onError("No puedes vender a crédito sin cliente")
                }

                // 🔥 ESTADO DE PAGO
                val estadoPago = when {
                    tipoVenta == "CONTADO" -> "PAGADO"
                    abonoInicial <= 0 -> "PENDIENTE"
                    abonoInicial < totalFinal -> "PARCIAL"
                    else -> "PAGADO"
                }

                // 🔥 SALDO PENDIENTE
                val saldoPendiente = when (tipoVenta) {
                    "CONTADO" -> 0.0
                    else -> totalFinal - abonoInicial
                }

                // 🔥 GUARDAR VENTA (NO SE TOCA)
                repository.guardarVenta(
                    clienteId = clienteId,
                    usuarioId = usuarioId,
                    items = snapshot,
                    miniBodegaId = miniBodegaId,
                    total = totalFinal,
                    tipoVenta = tipoVenta,
                    estadoPago = estadoPago,
                    saldoPendiente = saldoPendiente,
                    abonoInicial = abonoInicial
                )

                val ticketItems = snapshot.map {
                    TicketItem(
                        nombre = it.productoNombre,
                        presentacion = it.presentacionNombre,
                        cantidad = it.cantidad,
                        precioUnitario = it.precio
                    )
                }
                val fechaVenta = System.currentTimeMillis()

                val ticket = TicketBuilder.build(
                    items = ticketItems,
                    clienteNombre = cliente?.nombre,
                    clienteNegocio = cliente?.nombreNegocio,
                    subtotal = subtotal,
                    porcentajeDescuento = porcentaje,
                    descuento = descuento,
                    totalFinal = totalFinal,
                    fecha = fechaVenta,
                    tipoVenta = tipoVenta,
                    abonoInicial = abonoInicial
                )

                val device = bluetoothAdapter?.let {
                    printerRepository.getSavedPrinter(it)
                }

                delay(300)

                // 🔥 AQUÍ ESTÁ EL ÚNICO CAMBIO REAL
                val result = if (imprimir) {
                    withContext(Dispatchers.IO) {

                        val firstResult = printerManager.print(device, ticket)

                        // 🔥 Segunda copia (sin afectar tu lógica)
                        delay(300)
                        printerManager.print(device, ticket)

                        firstResult // 👈 respetamos el resultado original
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun verificarImpresora(): PrintResult {

        val device = bluetoothAdapter?.let {
            printerRepository.getSavedPrinter(it)
        }

        return printerManager.checkPrinter(device)
    }
}