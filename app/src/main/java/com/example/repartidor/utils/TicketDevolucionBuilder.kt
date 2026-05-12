package com.example.repartidor.utils

import com.example.repartidor.data.model.CarritoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketDevolucionBuilder {

    fun build(
        items: List<CarritoItem>,
        clienteNombre: String? = null,
        motivo: String,
        observacion: String,
        usuario: String?,
        fecha: Long
    ): String {

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaFormateada = sdf.format(Date(fecha))

        val sb = StringBuilder()

        sb.append("        OSMIT\n")
        sb.append("    DEVOLUCIÓN\n\n")

        sb.append("------------------------------\n")
        sb.append("Fecha: $fechaFormateada\n")
        sb.append("Tipo: DEVOLUCIÓN\n")
        sb.append("Motivo: $motivo\n")

        if (!observacion.isNullOrBlank()) {
            sb.append("Obs: $observacion\n")
        }

        if (clienteNombre != null) {
            sb.append("Cliente: $clienteNombre\n")
        }

        sb.append("Usuario: ${usuario ?: "N/A"}\n")

        sb.append("------------------------------\n")

        sb.append("PRODUCTOS:\n")

        items.forEach {
            sb.append("${it.cantidad}x ${it.productoNombre}\n")
            sb.append("   ${it.presentacionNombre}\n")
        }

        sb.append("------------------------------\n")
        sb.append("\n\n\n")

        return sb.toString()
    }
}