package com.example.repartidor.data.repository

import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.model.DetalleVentaUI
import com.example.repartidor.data.model.VentaUI
import com.example.repartidor.utils.convertirFechaATimestamp

class VentasDiaRepository(
    private val dao: VentaDao
) {

    suspend fun getVentasDelDia(usuarioId: Int): List<VentaUI> {
        val inicio = getInicioDelDia()
        val fin = getFinDelDia()

        val ventas = dao.getVentasDelDia(inicio, fin, usuarioId)

        return ventas.map { venta ->

            val cliente = venta.clienteId?.let {
                dao.getClienteById(it)
            }

            VentaUI(
                id = venta.id,
                nombreCliente = cliente?.nombre ?: "Venta rápida",
                nombreNegocio = cliente?.nombreNegocio,
                fecha = venta.fecha.toLongOrNull() ?: 0L,
                total = venta.total,
                porcentajeDescuento = cliente?.porcentajeDescuento,
                tipoVenta = venta.tipoVenta,           // 🔥

            )
        }
    }

    suspend fun getDetalleVenta(ventaId: Int): Pair<List<DetalleVentaUI>, Double> {

        val detalles = dao.getDetallesByVentaId(ventaId)

        val lista = detalles.map { detalle ->

            val variacion = dao.getProductoVariacionById(detalle.productoVariacionId)
            val presentacion = variacion?.presentacion?.let {
                dao.getPresentacionById(it)
            }

            val nombreCompleto = buildString {
                append(detalle.nombreProducto)
                presentacion?.nombre?.let {
                    append(" ")
                    append(it)
                }
            }

            DetalleVentaUI(
                nombreCompleto = nombreCompleto,
                cantidad = detalle.cantidad,
                precioUnitario = detalle.precioUnitario,
                subtotal = detalle.cantidad * detalle.precioUnitario
            )
        }

        // 🔥 AQUÍ LA MAGIA
        val totalAbonos = dao.getTotalAbonosByVentaId(ventaId) ?: 0.0

        return Pair(lista, totalAbonos)
    }

    private fun getInicioDelDia(): String {
        val start = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return start.timeInMillis.toString()
    }

    private fun getFinDelDia(): String {
        val end = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }
        return end.timeInMillis.toString()
    }
}