package com.example.repartidor.utils

import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.SustitucionSeleccionada
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketDevolucionBuilder {

    fun build(
        items: List<CarritoItem>,
        sustitucionesPorProducto: Map<Int, List<SustitucionSeleccionada>>,
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
        sb.append("Tipo: $motivo\n")

        if (!observacion.isNullOrBlank()) {
            sb.append("Obs: $observacion\n")
        }

        if (clienteNombre != null) {
            sb.append("Cliente: $clienteNombre\n")
        }

        sb.append("Repartidor: ${usuario ?: "N/A"}\n")

        sb.append("------------------------------\n")

        sb.append("PRODUCTOS:\n")

        items.forEach { item ->

            sb.append("${item.cantidad}x ${item.productoNombre}\n")
            sb.append("   ${item.presentacionNombre}\n")

            val sustituciones =
                sustitucionesPorProducto[item.productoVariacionId]

            if (!sustituciones.isNullOrEmpty()) {

                sb.append("   CAMBIO POR:\n")

                sustituciones.forEach { cambio ->
                    sb.append(
                        "   ${cambio.cantidad}x ${cambio.nombreProducto}" +
                                " - ${cambio.presentacionNombre}\n"
                    )
                }
            }
        }

        sb.append("------------------------------\n")
        sb.append("\n\n\n")

        return sb.toString()
    }
}