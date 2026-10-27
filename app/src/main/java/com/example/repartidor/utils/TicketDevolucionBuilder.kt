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
        fecha: Long,
        diferenciaDinero: Double = 0.0,
        tipoDiferencia: String = "SIN_DIFERENCIA",
        formaLiquidacion: String = "EFECTIVO"
    ): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaFormateada = sdf.format(Date(fecha))
        val sb = StringBuilder()

        sb.append("        OSMIT\n")
        sb.append("    DEVOLUCIÓN\n\n")
        sb.append("------------------------------\n")
        sb.append("Fecha: $fechaFormateada\n")
        sb.append("Tipo: $motivo\n")

        if (observacion.isNotBlank()) {
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

            val sustituciones = sustitucionesPorProducto[item.productoVariacionId]

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

        if (sustitucionesPorProducto.values.any { it.isNotEmpty() }) {
            sb.append("------------------------------\n")
            sb.append("DIFERENCIA ECONOMICA:\n")

            val importe = String.format(Locale.US, "%.2f", diferenciaDinero)

            when (tipoDiferencia) {
                "CLIENTE_PAGO" -> {
                    sb.append("Cliente paga: $$importe\n")
                    sb.append("Forma: $formaLiquidacion\n")
                }
                "REPARTIDOR_PAGO" -> {
                    sb.append("Se devuelve: $$importe\n")
                    sb.append("Forma: $formaLiquidacion\n")
                }
                else -> sb.append("Sin diferencia: $0.00\n")
            }
        }

        sb.append("------------------------------\n")
        sb.append("\n\n\n")

        return sb.toString()
    }
}