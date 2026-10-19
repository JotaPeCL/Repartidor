package com.example.repartidor.utils

import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.SustitucionRotacionSeleccionada
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object TicketRotacionBuilder {

    fun build(
        items: List<CarritoItem>,
        sustitucionesPorProducto: Map<Int, List<SustitucionRotacionSeleccionada>>,
        clienteNombre: String? = null,
        descripcion: String,
        usuario: String?,
        fecha: Long
    ): String {

        val sdf = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        val fechaFormateada = sdf.format(Date(fecha))

        val sb = StringBuilder()

        // =====================================
        // ENCABEZADO
        // =====================================

        sb.append("        OSMIT\n")
        sb.append("       ROTACION\n\n")

        sb.append("------------------------------\n")
        sb.append("Fecha: $fechaFormateada\n")

        if (clienteNombre != null) {
            sb.append("Cliente: $clienteNombre\n")
        } else {
            sb.append("Cliente: Venta rapida\n")
        }

        sb.append("Repartidor: ${usuario ?: "N/A"}\n")

        if (descripcion.isNotBlank()) {
            sb.append("Motivo: $descripcion\n")
        }

        sb.append("------------------------------\n")

        // =====================================
        // CALCULAR VALORES
        // =====================================

        val valorRecibidoCentavos = items.sumOf { item ->
            (item.precio * item.cantidad * 100.0).roundToLong()
        }

        val valorEntregadoCentavos = items.sumOf { item ->
            val sustituciones =
                sustitucionesPorProducto[item.productoVariacionId]
                    ?: emptyList()

            sustituciones.sumOf { sustitucion ->
                (
                        sustitucion.precio *
                                sustitucion.cantidad *
                                100.0
                        ).roundToLong()
            }
        }

        // =====================================
        // PRODUCTOS RECIBIDOS
        // =====================================

        sb.append("PRODUCTOS RECIBIDOS:\n\n")

        items.forEach { item ->

            sb.append(
                "${item.cantidad}x ${item.productoNombre}\n"
            )

            sb.append(
                "   ${item.presentacionNombre}\n"
            )
        }

        sb.append("\n")
        sb.append(
            "Valor recibido: $%.2f\n"
                .format(Locale.US, valorRecibidoCentavos / 100.0)
        )

        sb.append("------------------------------\n")

        // =====================================
        // PRODUCTOS ENTREGADOS
        // =====================================

        sb.append("PRODUCTOS ENTREGADOS:\n\n")

        items.forEach { item ->

            val sustituciones =
                sustitucionesPorProducto[item.productoVariacionId]
                    ?: emptyList()

            sustituciones.forEach { cambio ->

                sb.append(
                    "${cambio.cantidad}x ${cambio.nombreProducto}\n"
                )

                sb.append(
                    "   ${cambio.presentacionNombre}\n"
                )
            }
        }

        sb.append("\n")
        sb.append(
            "Valor entregado: $%.2f\n"
                .format(Locale.US, valorEntregadoCentavos / 100.0)
        )

        sb.append("------------------------------\n")

        // =====================================
        // DIFERENCIA DE DINERO
        // =====================================

        val diferenciaCentavos =
            valorEntregadoCentavos - valorRecibidoCentavos

        sb.append("DIFERENCIA DE DINERO:\n")

        when {
            diferenciaCentavos > 0 -> {
                sb.append(
                    "Cliente paga: $%.2f\n"
                        .format(Locale.US, diferenciaCentavos / 100.0)
                )
                sb.append("Forma: EFECTIVO\n")
            }

            diferenciaCentavos < 0 -> {
                sb.append(
                    "Repartidor entrega: $%.2f\n"
                        .format(Locale.US, abs(diferenciaCentavos) / 100.0)
                )
                sb.append("Forma: EFECTIVO\n")
            }

            else -> {
                sb.append("Sin diferencia de dinero\n")
            }
        }

        sb.append("------------------------------\n")
        sb.append("\n\n\n")

        return sb.toString()
    }
}
