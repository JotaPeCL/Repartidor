package com.example.repartidor.data.repository

import androidx.room.withTransaction
import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.local.CancelacionRotacionDao
import com.example.repartidor.data.local.CancelacionRotacionDetalleDao
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.RotacionDao
import com.example.repartidor.data.local.RotacionDetalleDao
import com.example.repartidor.data.local.RotacionSustitucionDao
import com.example.repartidor.data.model.entity.CancelacionRotacionEntity
import com.example.repartidor.data.model.entity.CancelacionRotacionDetalleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CancelacionRotacionRepository(
    private val database: AppDatabase,
    private val rotacionDao: RotacionDao,
    private val rotacionDetalleDao: RotacionDetalleDao,
    private val rotacionSustitucionDao: RotacionSustitucionDao,
    private val miniBodegaDetalleDao: MiniBodegaDetalleDao,
    private val cancelacionRotacionDao: CancelacionRotacionDao,
    private val cancelacionRotacionDetalleDao: CancelacionRotacionDetalleDao
) {

    suspend fun cancelarRotacion(
        rotacionId: Int,
        usuarioId: Int,
        miniBodegaId: Int,
        motivo: String
    ) {
        database.withTransaction {

            // =========================================================
            // VALIDACIONES INICIALES
            // =========================================================

            if (motivo.isBlank()) {
                throw Exception("Debes indicar el motivo de cancelación")
            }

            val rotacion = rotacionDao.getById(rotacionId)
                ?: throw Exception("La rotación no existe")

            if (rotacion.miniBodegaId != miniBodegaId) {
                throw Exception("La rotación pertenece a otra MiniBodega")
            }

            if (rotacion.usuarioId != usuarioId) {
                throw Exception("La rotación pertenece a otro repartidor")
            }

            if (cancelacionRotacionDao.existeCancelacion(rotacion.uuid)) {
                throw Exception("Esta rotación ya fue cancelada")
            }

            val detalles =
                rotacionDetalleDao.obtenerPorRotacion(rotacionId)

            val sustituciones =
                rotacionSustitucionDao.obtenerPorRotacion(rotacionId)

            if (detalles.isEmpty()) {
                throw Exception("La rotación no tiene productos registrados")
            }

            // =========================================================
            // CALCULAR MOVIMIENTOS NETOS DE INVENTARIO
            // =========================================================

            val ajustes = mutableMapOf<Int, Double>()

            // Los productos recibidos deben descontarse.
            detalles.forEach { detalle ->

                ajustes[detalle.productoVariacionId] =
                    (ajustes[detalle.productoVariacionId] ?: 0.0) -
                            detalle.cantidad
            }

            // Los productos entregados deben regresar al inventario.
            sustituciones.forEach { sustitucion ->

                ajustes[sustitucion.productoVariacionId] =
                    (ajustes[sustitucion.productoVariacionId] ?: 0.0) +
                            sustitucion.cantidad
            }

            // =========================================================
            // VALIDAR STOCK ANTES DE CANCELAR
            // =========================================================

            ajustes.forEach { (productoVariacionId, ajuste) ->

                val stockActual =
                    miniBodegaDetalleDao.obtenerStock(
                        miniBodegaId,
                        productoVariacionId
                    )

                if (stockActual == null) {
                    throw Exception(
                        "No existe el registro de inventario del producto $productoVariacionId"
                    )
                }

                if (stockActual + ajuste < -0.000001) {
                    throw Exception(
                        "No hay stock suficiente para cancelar la rotación. " +
                                "Producto: $productoVariacionId"
                    )
                }
            }

            // =========================================================
            // PREPARAR CANCELACIÓN
            // =========================================================

            val cancelacionUuid = UUID.randomUUID().toString()

            val fechaActual = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            // =========================================================
            // GUARDAR CANCELACIÓN
            // =========================================================

            val cancelacionId =
                cancelacionRotacionDao.insertar(
                    CancelacionRotacionEntity(
                        uuid = cancelacionUuid,
                        rotacionUuid = rotacion.uuid,
                        usuarioId = usuarioId,
                        miniBodegaId = miniBodegaId,
                        fecha = fechaActual,
                        descripcion = rotacion.descripcion,
                        diferenciaDinero = rotacion.diferenciaDinero,
                        tipoDiferencia = rotacion.tipoDiferencia,
                        formaLiquidacion = rotacion.formaLiquidacion,
                        motivo = motivo.trim(),
                        sincronizado = false,
                        createdAt = fechaActual
                    )
                ).toInt()

            // =========================================================
            // GUARDAR PRODUCTOS RECIBIDOS
            // =========================================================

            val detallesCancelacion =
                detalles.map { detalle ->

                    CancelacionRotacionDetalleEntity(
                        uuid = UUID.randomUUID().toString(),
                        cancelacionRotacionId = cancelacionId,
                        cancelacionRotacionUuid = cancelacionUuid,
                        productoVariacionId = detalle.productoVariacionId,
                        nombreProducto = detalle.nombreProducto,
                        cantidad = detalle.cantidad,
                        precioUnitario = detalle.precioUnitario,
                        tipoMovimiento = "RECIBIDO"
                    )
                }.toMutableList()

            // =========================================================
            // GUARDAR PRODUCTOS ENTREGADOS
            // =========================================================

            sustituciones.forEach { sustitucion ->

                detallesCancelacion.add(
                    CancelacionRotacionDetalleEntity(
                        uuid = UUID.randomUUID().toString(),
                        cancelacionRotacionId = cancelacionId,
                        cancelacionRotacionUuid = cancelacionUuid,
                        productoVariacionId = sustitucion.productoVariacionId,
                        nombreProducto = sustitucion.nombreProducto,
                        cantidad = sustitucion.cantidad,
                        precioUnitario = sustitucion.precioUnitario,
                        tipoMovimiento = "ENTREGADO"
                    )
                )
            }

            cancelacionRotacionDetalleDao.insertarDetalles(
                detallesCancelacion
            )

            // =========================================================
            // REVERTIR INVENTARIO
            // =========================================================

            ajustes.forEach { (productoVariacionId, ajuste) ->

                when {
                    ajuste > 0.000001 -> {
                        miniBodegaDetalleDao.aumentarStock(
                            miniBodegaId = miniBodegaId,
                            productoId = productoVariacionId,
                            cantidad = ajuste
                        )
                    }

                    ajuste < -0.000001 -> {
                        miniBodegaDetalleDao.descontarStock(
                            miniBodegaId = miniBodegaId,
                            productoId = productoVariacionId,
                            cantidad = -ajuste
                        )
                    }
                }
            }

            // =========================================================
            // ELIMINAR REGISTROS ORIGINALES
            // =========================================================

            rotacionSustitucionDao.eliminarPorRotacion(rotacionId)

            rotacionDetalleDao.eliminarPorRotacion(rotacionId)

            rotacionDao.eliminarPorId(rotacionId)
        }
    }
}