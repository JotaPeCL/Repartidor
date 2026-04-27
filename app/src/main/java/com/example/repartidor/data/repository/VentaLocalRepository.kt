package com.example.repartidor.data.repository

import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.model.VentaDetalleEntity
import com.example.repartidor.data.model.VentaEntity

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

        val ventaId = ventaDao.insertVenta(
            VentaEntity(
                clienteId = clienteId,
                usuarioId = usuarioId,
                fecha = System.currentTimeMillis().toString(),
                total = total
            )
        ).toInt()

        val detalles = items.map {
            VentaDetalleEntity(
                ventaId = ventaId,
                productoVariacionId = it.productoVariacionId,
                nombreProducto = it.productoNombre,
                cantidad = it.cantidad.toDouble(),
                precioUnitario = it.precio
            )
        }

        ventaDao.insertDetalles(detalles)

        // 🔥 Descontar stock
        items.forEach { item ->

            val stock = ventaDao.getProductoStock(
                miniBodegaId,
                item.productoVariacionId
            ) ?: throw Exception("Producto sin stock")

            val nuevaCantidad = stock.cantidadActual - item.cantidad

            if (nuevaCantidad < 0) {
                throw Exception("Stock insuficiente: ${item.productoNombre}")
            }

            ventaDao.updateMiniBodegaDetalle(
                stock.copy(cantidadActual = nuevaCantidad)
            )
        }
    }
}