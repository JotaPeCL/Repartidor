package com.example.repartidor.data.repository

import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.model.VentaDetalleEntity
import com.example.repartidor.data.model.VentaEntity
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
        total: Double
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
                tipoVenta = "CONTADO",        // 👈 nuevo
                estadoPago = "PAGADO",        // 👈 nuevo
                saldoPendiente = 0.0,         // 👈 nuevo
                fechaVencimiento = null       // 👈 nuevo
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
    }
}