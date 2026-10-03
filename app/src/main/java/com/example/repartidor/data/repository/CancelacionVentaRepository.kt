package com.example.repartidor.data.repository

import androidx.room.withTransaction
import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.local.CancelacionVentaDao
import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.model.entity.CancelacionVentaDetalleEntity
import com.example.repartidor.data.model.entity.CancelacionVentaEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CancelacionVentaRepository(
    private val database: AppDatabase,
    private val ventaDao: VentaDao,
    private val clienteDao: ClienteDao,
    private val miniBodegaDetalleDao: MiniBodegaDetalleDao,
    private val cancelacionVentaDao: CancelacionVentaDao
) {

    suspend fun cancelarVenta(
        ventaId: Int,
        usuarioId: Int,
        miniBodegaId: Int,
        motivo: String
    ) {
        database.withTransaction {

            val venta = ventaDao.getVentaById(ventaId)

            val detalles = ventaDao.getDetallesByVentaId(ventaId)

            val totalAbonos =
                ventaDao.getTotalAbonosByVentaId(ventaId) ?: 0.0

            val cancelacionUuid = UUID.randomUUID().toString()

            val fechaActual = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            // 1. Registrar la cancelación
            val cancelacionId = cancelacionVentaDao.insertarCancelacion(
                CancelacionVentaEntity(
                    uuid = cancelacionUuid,
                    ventaUuid = venta.uuid,
                    usuarioId = usuarioId,
                    miniBodegaId = miniBodegaId,
                    fecha = fechaActual,
                    total = venta.total,
                    motivo = motivo,
                    sincronizado = false,
                    createdAt = fechaActual
                )
            ).toInt()

            // 2. Guardar los productos cancelados
            val detallesCancelacion = detalles.map { detalle ->
                CancelacionVentaDetalleEntity(
                    uuid = UUID.randomUUID().toString(),
                    cancelacionVentaId = cancelacionId,
                    cancelacionVentaUuid = cancelacionUuid,
                    productoVariacionId = detalle.productoVariacionId,
                    nombreProducto = detalle.nombreProducto,
                    cantidad = detalle.cantidad,
                    precioUnitario = detalle.precioUnitario
                )
            }

            cancelacionVentaDao.insertarDetalles(detallesCancelacion)

            // 3. Regresar productos a la MiniBodega
            detalles.forEach { detalle ->
                miniBodegaDetalleDao.aumentarStock(
                    miniBodegaId = miniBodegaId,
                    productoId = detalle.productoVariacionId,
                    cantidad = detalle.cantidad
                )
            }

            // 4. Revertir la deuda del cliente
            if (venta.tipoVenta == "CREDITO" && venta.clienteId != null) {

                val deudaRevertir =
                    (venta.total - totalAbonos).coerceAtLeast(0.0)

                if (deudaRevertir > 0) {

                    val cliente = clienteDao.getClienteById(venta.clienteId)
                        ?: throw Exception("Cliente no encontrado")

                    val nuevoSaldo =
                        (cliente.saldoAdeudo - deudaRevertir).coerceAtLeast(0.0)

                    clienteDao.actualizarSaldo(
                        venta.clienteId,
                        nuevoSaldo
                    )
                }
            }

            // 5. Eliminar el abono inicial, si existe
            ventaDao.deleteAbonosByVentaId(ventaId)

            // 6. Eliminar los detalles de la venta
            ventaDao.deleteDetallesByVentaId(ventaId)

            // 7. Eliminar la venta
            ventaDao.deleteVentaById(ventaId)
        }
    }
}