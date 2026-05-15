package com.example.repartidor.data.repository

import com.example.repartidor.data.local.DevolucionDao
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.MiniBodegaDetalleMermaDao
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.model.DevolucionDetalleEntity
import com.example.repartidor.data.model.DevolucionEntity
import com.example.repartidor.data.model.MiniBodegaDetalleMermaEntity
import java.util.UUID
import kotlin.collections.map

class DevolucionInventarioRepository(
    private val devolucionDao: DevolucionDao,
    private val miniBodegaDetalleDao: MiniBodegaDetalleDao,
    private val mermaDao: MiniBodegaDetalleMermaDao
) {

    @androidx.room.Transaction
    suspend fun registrarDevolucion(
        devolucion: DevolucionEntity,
        carrito: List<CarritoItem>
    ) {

        // ─────────────────────────────
        // 1. VALIDAR STOCK REAL
        // ─────────────────────────────
        carrito.forEach { item ->

            val stock = miniBodegaDetalleDao.obtenerStock(
                devolucion.miniBodegaId,
                item.productoVariacionId
            ) ?: 0.0

            if (stock < item.cantidad) {
                throw Exception("Stock insuficiente para ${item.productoNombre}")
            }
        }

        // ─────────────────────────────
        // 2. INSERT DEVOLUCIÓN
        // ─────────────────────────────
        val devolucionId = devolucionDao
            .insertarDevolucion(devolucion)
            .toInt()

        // ─────────────────────────────
        // 3. INSERT DETALLES
        // ─────────────────────────────
        val detalles = carrito.map { item ->
            DevolucionDetalleEntity(
                uuid = UUID.randomUUID().toString(),
                devolucionId = devolucionId,
                devolucionUuid = devolucion.uuid,
                productoVariacionId = item.productoVariacionId,
                nombreProducto = item.productoNombre,
                cantidad = item.cantidad.toDouble(),
                precioUnitario = item.precio
            )
        }

        devolucionDao.insertarDetalles(detalles)

        // ─────────────────────────────
        // 4. DESCONTAR STOCK
        // ─────────────────────────────
        carrito.forEach { item ->
            miniBodegaDetalleDao.descontarStock(
                miniBodegaId = devolucion.miniBodegaId,
                productoId = item.productoVariacionId,
                cantidad = item.cantidad.toDouble()
            )
        }

        // ─────────────────────────────
        // 5. REGISTRAR MERMA
        // ─────────────────────────────
        carrito.forEach { item ->

            val merma = MiniBodegaDetalleMermaEntity(
                uuid = UUID.randomUUID().toString(),
                miniBodegaId = devolucion.miniBodegaId,
                productoVariacionId = item.productoVariacionId,
                cantidad = item.cantidad.toDouble(),
                devolucionId = devolucionId,
                devolucionUuid = devolucion.uuid,
                createdAt = devolucion.createdAt
            )

            mermaDao.insertarMerma(merma)
        }
    }
}