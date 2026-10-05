package com.example.repartidor.data.repository

import com.example.repartidor.data.local.DevolucionDao
import com.example.repartidor.data.local.DevolucionSustitucionDao
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.MiniBodegaDetalleMermaDao
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.dclass.SustitucionSeleccionada
import com.example.repartidor.data.model.entity.DevolucionDetalleEntity
import com.example.repartidor.data.model.entity.DevolucionEntity
import com.example.repartidor.data.model.entity.DevolucionSustitucionEntity
import com.example.repartidor.data.model.entity.MiniBodegaDetalleMermaEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DevolucionInventarioRepository(
    private val devolucionDao: DevolucionDao,
    private val devolucionSustitucionDao: DevolucionSustitucionDao,
    private val miniBodegaDetalleDao: MiniBodegaDetalleDao,
    private val mermaDao: MiniBodegaDetalleMermaDao
) {

    fun getVariacionesDisponiblesParaSustitucion(
        miniBodegaId: Int
    ): Flow<List<ProductoSustitucion>> {
        return miniBodegaDetalleDao.getVariacionesDisponiblesParaSustitucion(miniBodegaId)
    }

    @androidx.room.Transaction
    suspend fun registrarDevolucion(
        devolucion: DevolucionEntity,
        carrito: List<CarritoItem>,
        sustitucionesPorProducto: Map<Int, List<SustitucionSeleccionada>>
    ) {

        // ─────────────────────────────
        // 1. VALIDAR DEVOLUCIONES
        // ─────────────────────────────
        carrito.forEach { item ->

            val sustituciones =
                sustitucionesPorProducto[item.productoVariacionId]
                    ?: emptyList()

            // Si tiene cambio, debe ser una devolución de cliente
            if (sustituciones.isNotEmpty()) {

                if (devolucion.tipo != "Devolución de cliente") {
                    throw Exception(
                        "Solo se puede realizar un cambio en una devolución de cliente"
                    )
                }

                // Valor total del producto devuelto
                val valorDevuelto =
                    item.cantidad.toDouble() * item.precio

                // Valor total de los productos de cambio
                val valorCambio =
                    sustituciones.sumOf {
                        it.cantidad.toDouble() * it.precio
                    }

                // El cambio debe tener exactamente el mismo valor
                if (kotlin.math.abs(valorDevuelto - valorCambio) > 0.01) {
                    throw Exception(
                        "El valor de los productos de cambio debe ser igual " +
                                "al valor del producto devuelto"
                    )
                }

            } else {

                // ─────────────────────────────
                // DEVOLUCIÓN NORMAL
                // ─────────────────────────────

                val stock = miniBodegaDetalleDao.obtenerStock(
                    devolucion.miniBodegaId,
                    item.productoVariacionId
                ) ?: 0.0

                if (stock < item.cantidad) {
                    throw Exception("Stock insuficiente para ${item.productoNombre}"
                    )
                }
            }
        }

        // ─────────────────────────────
        // 2. VALIDAR STOCK DE PRODUCTOS
        //    DE CAMBIO
        // ─────────────────────────────
        val sustitucionesTotales = mutableMapOf<Int, Double>()

        sustitucionesPorProducto.values
            .flatten()
            .forEach { sustitucion ->

                sustitucionesTotales[sustitucion.productoVariacionId] =
                    (sustitucionesTotales[sustitucion.productoVariacionId] ?: 0.0) +
                            sustitucion.cantidad.toDouble()
            }

        sustitucionesTotales.forEach { (productoVariacionId, cantidad) ->

            val stock = miniBodegaDetalleDao.obtenerStock(
                devolucion.miniBodegaId,
                productoVariacionId
            ) ?: 0.0

            if (stock < cantidad) {
                throw Exception(
                    "Stock insuficiente para el producto de cambio"
                )
            }
        }

        // ─────────────────────────────
        // 3. INSERT DEVOLUCIÓN
        // ─────────────────────────────
        val devolucionId = devolucionDao
            .insertarDevolucion(devolucion)
            .toInt()

        // ─────────────────────────────
        // 4. INSERT DETALLES
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

        val detalleIds = devolucionDao.insertarDetalles(detalles)

        // ─────────────────────────────
        // 5. INSERT SUSTITUCIONES
        // ─────────────────────────────
        val sustitucionesEntities = mutableListOf<DevolucionSustitucionEntity>()

        carrito.forEachIndexed { index, item ->

            val sustituciones =
                sustitucionesPorProducto[item.productoVariacionId]
                    ?: emptyList()

            val devolucionDetalleId =
                detalleIds[index].toInt()

            sustituciones.forEach { sustitucion ->

                sustitucionesEntities.add(
                    DevolucionSustitucionEntity(
                        uuid = UUID.randomUUID().toString(),
                        devolucionId = devolucionId,
                        devolucionUuid = devolucion.uuid,
                        devolucionDetalleId = devolucionDetalleId,
                        productoVariacionId = sustitucion.productoVariacionId,
                        nombreProducto = sustitucion.nombreProducto,
                        cantidad = sustitucion.cantidad.toDouble(),
                        precioUnitario = sustitucion.precio,
                        createdAt = devolucion.createdAt,
                        sincronizado = false
                    )
                )
            }
        }

        if (sustitucionesEntities.isNotEmpty()) {
            devolucionSustitucionDao.insertarSustituciones(
                sustitucionesEntities
            )
        }

        // ─────────────────────────────
        // 6. DESCONTAR STOCK DE
        //    DEVOLUCIONES NORMALES
        // ─────────────────────────────
        carrito.forEach { item ->

            val sustituciones =
                sustitucionesPorProducto[item.productoVariacionId]
                    ?: emptyList()

            // Si tiene cambio, NO se descuenta
            // stock del producto devuelto
            if (sustituciones.isEmpty()) {

                miniBodegaDetalleDao.descontarStock(
                    miniBodegaId = devolucion.miniBodegaId,
                    productoId = item.productoVariacionId,
                    cantidad = item.cantidad.toDouble()
                )
            }
        }

        // ─────────────────────────────
        // 7. DESCONTAR STOCK DE CAMBIOS
        // ─────────────────────────────
        sustitucionesTotales.forEach { (productoVariacionId, cantidad) ->

            miniBodegaDetalleDao.descontarStock(
                miniBodegaId = devolucion.miniBodegaId,
                productoId = productoVariacionId,
                cantidad = cantidad
            )
        }

        // ─────────────────────────────
        // 8. REGISTRAR MERMA
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
