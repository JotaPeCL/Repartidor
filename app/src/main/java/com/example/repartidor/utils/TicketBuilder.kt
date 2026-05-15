package com.example.repartidor.utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TicketItem(
    val cantidad: Int,
    val nombre: String,
    val presentacion: String, // 🔥 nuevo
    val precioUnitario: Double
)
object TicketBuilder {

    fun build(
        items: List<TicketItem>,
        clienteNombre: String? = null,
        clienteNegocio: String? = null,
        subtotal: Double,
        porcentajeDescuento: Double,
        descuento: Double,
        totalFinal: Double,
        fecha:Long,
        tipoVenta: String,
        abonoInicial: Double = 0.0
    ): String {

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaFormateada = sdf.format(Date(fecha))

        val sb = StringBuilder()

        sb.append("        OSMIT\n")
        sb.append("        VENTAS\n\n")

        sb.append("Ticket de Venta\n")
        sb.append("------------------------------\n")
        sb.append("Fecha: $fechaFormateada\n")
        sb.append("Tipo de venta: $tipoVenta\n")

        // 🔥 CLIENTE (solo si existe)
        if (clienteNombre != null) {
            sb.append("Cliente: $clienteNombre\n")
            clienteNegocio?.let {
                sb.append("Negocio: $it\n")
            }
        }

        sb.append("------------------------------\n")

        // 🔹 DETALLE DE PRODUCTOS
        items.forEach { item ->

            val sub = item.cantidad * item.precioUnitario

            sb.append("${item.cantidad}x ${item.nombre} - ${item.presentacion}\n")
            sb.append("    Subtotal: $${String.format("%.2f", sub)}\n")
        }

        sb.append("------------------------------\n")

        // 🔹 SUBTOTAL GENERAL
        sb.append("SUBTOTAL: $${String.format("%.2f", subtotal)}\n")

        // 🔹 DESCUENTO (solo si aplica)
        if (descuento > 0) {
            sb.append("DESCUENTO (${String.format("%.0f", porcentajeDescuento)}%): -$${String.format("%.2f", descuento)}\n")
        }

        // 🔹 TOTAL FINAL
        sb.append("TOTAL: $${String.format("%.2f", totalFinal)}\n")

        if (tipoVenta == "CREDITO") {

            val restante = (totalFinal - abonoInicial).coerceAtLeast(0.0)

            sb.append("------------------------------\n")
            sb.append("PAGO INICIAL: $${String.format("%.2f", abonoInicial)}\n")
            sb.append("SALDO PENDIENTE: $${String.format("%.2f", restante)}\n")
            sb.append("\n\n")
            sb.append("------------------------------\n")
            sb.append("\n\n")
            sb.append("______________________________\n")
            sb.append("            FIRMA\n")
        }
        sb.append("\n\n\n")

        return sb.toString()
    }
}