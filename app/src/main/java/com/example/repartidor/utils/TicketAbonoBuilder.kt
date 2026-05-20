package com.example.repartidor.utils

object TicketAbonoBuilder {

    fun build(
        cliente: String,
        negocio: String,
        total: Double,
        abono: Double,
        saldoRestante: Double,
        fecha: Long
    ): String {

        return buildString {
            appendLine("        OSMIT")
            appendLine("        ABONO\n")
            appendLine("Cliente: $cliente")
            appendLine("Negocio: $negocio")
            appendLine("Fecha: ${formatearFecha(fecha)}")

            appendLine("-----------------------------")
            appendLine("Total: $${"%.2f".format(total)}")
            appendLine("Abono: $${"%.2f".format(abono)}")
            appendLine("Saldo restante: $${"%.2f".format(saldoRestante)}")
            appendLine("-----------------------------")
            appendLine("Gracias por su pago")
            appendLine("\n\n\n")

        }
    }

    private fun formatearFecha(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
        return sdf.format(java.util.Date(timestamp))
    }
}