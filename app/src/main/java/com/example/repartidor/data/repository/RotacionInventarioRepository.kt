package com.example.repartidor.data.repository

import androidx.room.withTransaction
import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.RotacionDao
import com.example.repartidor.data.local.RotacionDetalleDao
import com.example.repartidor.data.local.RotacionSustitucionDao
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.SustitucionRotacionSeleccionada
import com.example.repartidor.data.model.entity.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.entity.RotacionDetalleEntity
import com.example.repartidor.data.model.entity.RotacionEntity
import com.example.repartidor.data.model.entity.RotacionSustitucionEntity
import java.util.UUID
import kotlin.math.abs

class RotacionInventarioRepository(
    private val database: AppDatabase,
    private val rotacionDao: RotacionDao,
    private val rotacionDetalleDao: RotacionDetalleDao,
    private val rotacionSustitucionDao: RotacionSustitucionDao,
    private val miniBodegaDetalleDao: MiniBodegaDetalleDao
) {

    suspend fun registrarRotacion(
        rotacion: RotacionEntity,
        carrito: List<CarritoItem>,
        sustitucionesPorProducto: Map<Int, List<SustitucionRotacionSeleccionada>>
    ) {
        database.withTransaction {

            // =========================================================
            // VALIDAR VALORES
            // =========================================================

            carrito.forEach { item ->

                val sustituciones =
                    sustitucionesPorProducto[item.productoVariacionId]
                        ?: emptyList()

                if (sustituciones.isEmpty()) {
                    throw Exception(
                        "Debes seleccionar productos de cambio para ${item.productoNombre}"
                    )
                }

                val valorRecibido =
                    item.cantidad.toDouble() * item.precio

                val valorEntregado =
                    sustituciones.sumOf {
                        it.cantidad.toDouble() * it.precio
                    }

                if (abs(valorRecibido - valorEntregado) > 0.01) {
                    throw Exception(
                        "El valor de los productos de cambio debe ser igual " +
                                "al valor del producto rotado"
                    )
                }
            }

            // =========================================================
            // AGRUPAR STOCK DE SUSTITUCIONES
            // =========================================================

            val sustitucionesTotales = mutableMapOf<Int, Double>()

            sustitucionesPorProducto.values
                .flatten()
                .forEach { sustitucion ->

                    sustitucionesTotales[sustitucion.productoVariacionId] =
                        (sustitucionesTotales[sustitucion.productoVariacionId] ?: 0.0) +
                                sustitucion.cantidad.toDouble()
                }

            // =========================================================
            // VALIDAR STOCK
            // =========================================================

            sustitucionesTotales.forEach { (productoVariacionId, cantidad) ->

                val stock =
                    miniBodegaDetalleDao.obtenerStock(
                        rotacion.miniBodegaId,
                        productoVariacionId
                    ) ?: 0.0

                if (stock < cantidad) {
                    throw Exception(
                        "Stock insuficiente para el producto de cambio"
                    )
                }
            }

            // =========================================================
            // GUARDAR ROTACIÓN
            // =========================================================

            val rotacionId =
                rotacionDao.insert(rotacion).toInt()

            // =========================================================
            // GUARDAR DETALLES
            // =========================================================

            val detalles = carrito.map { item ->

                RotacionDetalleEntity(
                    uuid = UUID.randomUUID().toString(),
                    rotacionId = rotacionId,
                    rotacionUuid = rotacion.uuid,
                    productoVariacionId = item.productoVariacionId,
                    nombreProducto = item.productoNombre,
                    cantidad = item.cantidad.toDouble(),
                    precioUnitario = item.precio
                )
            }

            val detalleIds =
                rotacionDetalleDao.insertarDetalles(detalles)

            // =========================================================
            // GUARDAR SUSTITUCIONES
            // =========================================================

            val sustitucionesEntities =
                mutableListOf<RotacionSustitucionEntity>()

            carrito.forEachIndexed { index, item ->

                val sustituciones =
                    sustitucionesPorProducto[item.productoVariacionId]
                        ?: emptyList()

                val detalleId =
                    detalleIds[index].toInt()

                sustituciones.forEach { sustitucion ->

                    sustitucionesEntities.add(
                        RotacionSustitucionEntity(
                            uuid = UUID.randomUUID().toString(),
                            rotacionId = rotacionId,
                            rotacionUuid = rotacion.uuid,
                            rotacionDetalleId = detalleId,
                            productoVariacionId =
                                sustitucion.productoVariacionId,
                            nombreProducto =
                                sustitucion.nombreProducto,
                            cantidad =
                                sustitucion.cantidad.toDouble(),
                            precioUnitario =
                                sustitucion.precio,
                            createdAt =
                                rotacion.createdAt,
                            sincronizado = false
                        )
                    )
                }
            }

            if (sustitucionesEntities.isNotEmpty()) {

                rotacionSustitucionDao.insertarSustituciones(
                    sustitucionesEntities
                )
            }

            // =========================================================
            // RECIBIR PRODUCTOS DEL CLIENTE
            // =========================================================

            carrito.forEach { item ->

                val stockActual =
                    miniBodegaDetalleDao.obtenerStock(
                        rotacion.miniBodegaId,
                        item.productoVariacionId
                    )

                if (stockActual != null) {

                    miniBodegaDetalleDao.aumentarStock(
                        miniBodegaId = rotacion.miniBodegaId,
                        productoId = item.productoVariacionId,
                        cantidad = item.cantidad.toDouble()
                    )

                } else {

                    val idTemporal =
                        generarIdTemporal()

                    miniBodegaDetalleDao.insertAll(
                        listOf(
                            MiniBodegaDetalleEntity(
                                id = idTemporal,
                                miniBodegaId = rotacion.miniBodegaId,
                                productoVariacionId =
                                    item.productoVariacionId,
                                cantidadInicial = 0.0,
                                cantidadActual =
                                    item.cantidad.toDouble(),
                                updatedAt = rotacion.createdAt
                            )
                        )
                    )
                }
            }

            // =========================================================
            // ENTREGAR PRODUCTOS AL CLIENTE
            // =========================================================

            sustitucionesTotales.forEach { (productoVariacionId, cantidad) ->

                miniBodegaDetalleDao.descontarStock(
                    miniBodegaId = rotacion.miniBodegaId,
                    productoId = productoVariacionId,
                    cantidad = cantidad
                )
            }
        }
    }

    private suspend fun generarIdTemporal(): Int {

        val ids =
            miniBodegaDetalleDao
                .getAllIds()
                .toSet()

        var id = -1

        while (id in ids) {
            id--
        }

        return id
    }
}