package com.example.repartidor.data.repository

import androidx.room.withTransaction
import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.local.CancelacionDevolucionDao
import com.example.repartidor.data.local.DevolucionDao
import com.example.repartidor.data.local.DevolucionDetalleDao
import com.example.repartidor.data.local.DevolucionSustitucionDao
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.MiniBodegaDetalleMermaDao
import com.example.repartidor.data.model.entity.CancelacionDevolucionDetalleEntity
import com.example.repartidor.data.model.entity.CancelacionDevolucionEntity
import com.example.repartidor.data.model.entity.CancelacionDevolucionSustitucionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CancelacionDevolucionRepository(
    private val database: AppDatabase,
    private val devolucionDao: DevolucionDao,
    private val devolucionDetalleDao: DevolucionDetalleDao,
    private val devolucionSustitucionDao: DevolucionSustitucionDao,
    private val miniBodegaDetalleDao: MiniBodegaDetalleDao,
    private val mermaDao: MiniBodegaDetalleMermaDao,
    private val cancelacionDevolucionDao: CancelacionDevolucionDao
) {

    suspend fun cancelarDevolucion(
        devolucionId: Int,
        usuarioId: Int,
        miniBodegaId: Int,
        motivo: String
    ) {
        database.withTransaction {

            val devolucion = devolucionDao.getById(devolucionId)
                ?: throw Exception("Devolución no encontrada")

            val detalles =
                devolucionDetalleDao.getByDevolucion(devolucionId)

            val sustituciones =
                devolucionSustitucionDao.obtenerPorDevolucion(
                    devolucionId
                )

            val cancelacionUuid = UUID.randomUUID().toString()

            val fechaActual = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            val total = detalles.sumOf {
                it.cantidad * it.precioUnitario
            }

            val cancelacionId =
                cancelacionDevolucionDao.insertarCancelacion(
                    CancelacionDevolucionEntity(
                        uuid = cancelacionUuid,
                        devolucionUuid = devolucion.uuid,
                        usuarioId = usuarioId,
                        miniBodegaId = miniBodegaId,
                        fecha = fechaActual,
                        total = total,
                        motivo = motivo,
                        sincronizado = false,
                        createdAt = fechaActual
                    )
                ).toInt()

            val detallesCancelacion =
                detalles.map { detalle ->

                    CancelacionDevolucionDetalleEntity(
                        uuid = UUID.randomUUID().toString(),
                        cancelacionDevolucionId = cancelacionId,
                        cancelacionDevolucionUuid = cancelacionUuid,
                        productoVariacionId =
                            detalle.productoVariacionId,
                        nombreProducto =
                            detalle.nombreProducto,
                        cantidad =
                            detalle.cantidad,
                        precioUnitario =
                            detalle.precioUnitario
                    )
                }

            cancelacionDevolucionDao.insertarDetalles(
                detallesCancelacion
            )

            val sustitucionesCancelacion =
                sustituciones.map { sustitucion ->

                    CancelacionDevolucionSustitucionEntity(
                        uuid = UUID.randomUUID().toString(),
                        cancelacionDevolucionId = cancelacionId,
                        cancelacionDevolucionUuid = cancelacionUuid,
                        productoVariacionId =
                            sustitucion.productoVariacionId,
                        nombreProducto =
                            sustitucion.nombreProducto,
                        cantidad =
                            sustitucion.cantidad,
                        precioUnitario =
                            sustitucion.precioUnitario
                    )
                }

            if (sustitucionesCancelacion.isNotEmpty()) {
                cancelacionDevolucionDao.insertarSustituciones(
                    sustitucionesCancelacion
                )
            }

            /*
             * Revertir inventario por cada detalle de devolución.
             *
             * Si el detalle NO tiene sustituciones:
             *   El producto devuelto había sido descontado
             *   de MiniBodega, por lo que debemos regresarlo.
             *
             * Si el detalle SÍ tiene sustituciones:
             *   El producto devuelto no fue descontado de
             *   MiniBodega, por lo que NO debemos regresarlo.
             */
            detalles.forEach { detalle ->

                val sustitucionesDelDetalle =
                    sustituciones.filter {
                        it.devolucionDetalleId == detalle.id
                    }

                if (sustitucionesDelDetalle.isEmpty()) {

                    miniBodegaDetalleDao.aumentarStock(
                        miniBodegaId = miniBodegaId,
                        productoId =
                            detalle.productoVariacionId,
                        cantidad =
                            detalle.cantidad
                    )
                }
            }

            /*
             * Los productos sustitutos sí fueron descontados
             * originalmente de MiniBodega.
             *
             * Al cancelar la devolución debemos regresarlos.
             */
            sustituciones.forEach { sustitucion ->

                miniBodegaDetalleDao.aumentarStock(
                    miniBodegaId = miniBodegaId,
                    productoId =
                        sustitucion.productoVariacionId,
                    cantidad =
                        sustitucion.cantidad
                )
            }

            /*
             * La devolución creó las mermas de los productos
             * devueltos.
             *
             * Al cancelar la devolución, esas mermas dejan de existir.
             */
            mermaDao.eliminarPorDevolucionId(
                devolucionId
            )

            /*
             * Eliminar las sustituciones originales.
             *
             * La información ya quedó registrada en
             * cancelacion_devolucion_sustitucion.
             */
            devolucionSustitucionDao.eliminarPorDevolucion(
                devolucionId
            )

            /*
             * Eliminar los detalles originales.
             *
             * La información ya quedó registrada en
             * cancelacion_devolucion_detalle.
             */
            devolucionDetalleDao.deleteByDevolucionId(
                devolucionId
            )

            /*
             * Finalmente eliminar la devolución original.
             *
             * La cancelación queda conservada para sincronización
             * y auditoría.
             */
            devolucionDao.deleteById(
                devolucionId
            )
        }
    }
}