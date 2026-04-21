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
        clienteNegocio: String? = null
    ): String {

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = sdf.format(Date())

        val sb = StringBuilder()

        sb.append("        OSMIT\n")
        sb.append("        VENTAS\n\n")

        sb.append("Ticket de Venta\n")
        sb.append("------------------------------\n")
        sb.append("Fecha: $fecha\n")

        // 🔥 CLIENTE (solo si existe)
        if (clienteNombre != null) {
            sb.append("Cliente: $clienteNombre\n")
            clienteNegocio?.let {
                sb.append("Negocio: $it\n")
            }
        }

        sb.append("------------------------------\n")

        var total = 0.0

        items.forEach { item ->

            val subtotal = item.cantidad * item.precioUnitario
            total += subtotal

            sb.append("${item.cantidad}x ${item.nombre} - ${item.presentacion}\n")
            sb.append("    Subtotal: $${String.format("%.2f", subtotal)}\n")
        }

        sb.append("------------------------------\n")
        sb.append("TOTAL: $${String.format("%.2f", total)}\n")
        sb.append("\n\n\n")

        return sb.toString()
    }
}