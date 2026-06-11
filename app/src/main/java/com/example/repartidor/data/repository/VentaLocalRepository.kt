package com.example.repartidor.data.repository

import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.model.entity.AbonoEntity
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.entity.ClienteEntity
import com.example.repartidor.data.model.entity.VentaDetalleEntity
import com.example.repartidor.data.model.entity.VentaEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class VentaLocalRepository(
    private val ventaDao: VentaDao,
    private val clienteDao: ClienteDao
) {

    suspend fun getClienteById(id: Int): ClienteEntity? {
        return clienteDao.getClienteById(id)
    }

    suspend fun guardarVenta(
        clienteId: Int?,
        usuarioId: Int,
        items: List<CarritoItem>,
        miniBodegaId: Int,
        total: Double,
        tipoVenta: String,
        estadoPago: String,
        saldoPendiente: Double,
        abonoInicial: Double
    ) {

        // 🔥 1. VALIDAR STOCK PRIMERO
        items.forEach { item ->

            val stock = ventaDao.getProductoStock(
                miniBodegaId,
                item.productoVariacionId
            ) ?: throw Exception("Producto sin stock")

            val nuevaCantidad = stock.cantidadActual - item.cantidad

            if (nuevaCantidad < 0) {
                throw Exception("Stock insuficiente: ${item.productoNombre}")
            }
        }
        val ventaUuid = UUID.randomUUID().toString()

        // 🔥 2. SI TODO OK → GUARDAR
        val ventaId = ventaDao.insertVenta(
            VentaEntity(
                uuid = ventaUuid,
                clienteId = clienteId,
                usuarioId = usuarioId,
                fecha = System.currentTimeMillis().toString(),
                total = total,
                //Simulado de mientras
                tipoVenta = tipoVenta,
                estadoPago = estadoPago,
                saldoPendiente = saldoPendiente,
                fechaVencimiento = null
            )
        ).toInt()

        val detalles = items.map {
            VentaDetalleEntity(
                uuid = UUID.randomUUID().toString(),
                ventaId = ventaId,
                ventaUuid = ventaUuid,
                productoVariacionId = it.productoVariacionId,
                nombreProducto = it.productoNombre,
                cantidad = it.cantidad.toDouble(),
                precioUnitario = it.precio
            )
        }

        ventaDao.insertDetalles(detalles)

        if (abonoInicial > 0) {
            val abono = AbonoEntity(
                uuid = UUID.randomUUID().toString(),
                ventaId = ventaId,
                ventaUuid = ventaUuid,
                usuarioId = usuarioId,
                monto = abonoInicial,
                fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                sincronizado = false
            )

            ventaDao.insertAbono(abono)
        }

        // 🔥 3. DESCONTAR STOCK
        items.forEach { item ->
            val stock = ventaDao.getProductoStock(
                miniBodegaId,
                item.productoVariacionId
            )!!

            val nuevaCantidad = stock.cantidadActual - item.cantidad

            ventaDao.updateMiniBodegaDetalle(
                stock.copy(cantidadActual = nuevaCantidad)
            )
        }

        if (tipoVenta == "CREDITO" && clienteId != null) {

            val cliente = clienteDao.getClienteById(clienteId)
                ?: throw Exception("Cliente no encontrado")

            val deudaGenerada = (total - abonoInicial).coerceAtLeast(0.0)

            if (deudaGenerada > 0) {

                val nuevoSaldo = cliente.saldoAdeudo + deudaGenerada

                clienteDao.actualizarSaldo(clienteId, nuevoSaldo)
            }
        }

    }
}