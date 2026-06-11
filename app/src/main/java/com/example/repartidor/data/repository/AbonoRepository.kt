package com.example.repartidor.data.repository

import androidx.room.withTransaction
import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.entity.AbonoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AbonoRepository(
    private val db: AppDatabase
) {

    suspend fun registrarAbono(
        ventaId: Int,
        monto: Double,
        usuarioId: Int
    ) {
        db.withTransaction {

            val venta = db.ventaDao().getVentaById(ventaId)

            // 🚫 Validaciones reales
            if (monto <= 0.0) return@withTransaction
            if (monto > venta.saldoPendiente) return@withTransaction

            val nuevoSaldo = (venta.saldoPendiente - monto).coerceAtLeast(0.0)

            // 🧠 Estado igual que Django
            val nuevoEstado = when {
                nuevoSaldo <= 0.0 -> "PAGADO"
                nuevoSaldo < venta.total -> "PARCIAL"
                else -> "PENDIENTE"
            }

            // 🔄 Actualizar venta
            val ventaActualizada = venta.copy(
                saldoPendiente = nuevoSaldo,
                estadoPago = nuevoEstado,
                sincronizado = false
            )

            db.ventaDao().updateVenta(ventaActualizada)

            // 🔄 Actualizar cliente (si existe)
            venta.clienteId?.let { clienteId ->
                val cliente = db.clienteDao().getClienteById2(clienteId)

                val nuevoAdeudo = (cliente.saldoAdeudo.minus(monto)).coerceAtLeast(0.0)

                val clienteActualizado = cliente.copy(
                    saldoAdeudo = nuevoAdeudo
                )

                db.clienteDao().updateCliente(clienteActualizado)
            }

            // 🧾 Crear abono
            val abono = AbonoEntity(
                uuid = UUID.randomUUID().toString(),
                ventaId = venta.id,
                ventaUuid = venta.uuid,
                usuarioId = usuarioId,
                monto = monto,
                fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                sincronizado = false
            )

            db.abonoDao().insertAbono(abono)
        }
    }
}