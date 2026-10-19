
package com.example.repartidor.utils

import com.example.repartidor.data.model.dclass.InventarioItem
import com.example.repartidor.data.model.dclass.ResumenDiaState

object ResumenTicketBuilder {

    fun build(
        state: ResumenDiaState,
        inventario: List<InventarioItem>,
        nombreUsuario: String,
        ruta: String,
        fecha: String,
        imprimirProductos: Boolean,
        imprimirDinero: Boolean,
        imprimirInventario: Boolean,
        imprimirDevoluciones: Boolean,
        imprimirRotaciones: Boolean
    ): String {

        val sb = StringBuilder()

        sb.append("\n")
        sb.append("===== RESUMEN DEL DIA =====\n")
        sb.append("Usuario: $nombreUsuario\n")
        sb.append("Ruta: $ruta\n")
        sb.append("Fecha: $fecha\n")

        // 🔹 PRODUCTOS VENDIDOS
        if (imprimirProductos) {
            sb.append("--- PRODUCTOS VENDIDOS ---\n")

            state.productosVendidos.forEach {
                sb.append("${it.nombre}: ${it.cantidad}\n")
            }

            sb.append("\n")
        }

        // 🔹 DINERO
        if (imprimirDinero) {
            sb.append("--- DINERO ---\n")

            sb.append(
                "Ventas totales: %.2f\n".format(state.dineroTotalVentas)
            )

            sb.append(
                "Efectivo en ventas: %.2f\n".format(state.efectivoVentas)
            )

            sb.append(
                "Abonos: %.2f\n".format(state.totalAbonos)
            )

            // Diferencias de dinero por rotaciones
            if (state.cantidadRotaciones > 0) {

                sb.append(
                    "Recibido rotaciones: +%.2f\n"
                        .format(state.totalRotacionesRecibido)
                )

                sb.append(
                    "Entregado rotaciones: -%.2f\n"
                        .format(state.totalRotacionesEntregado)
                )

                sb.append(
                    "Balance rotaciones: %+.2f\n"
                        .format(state.balanceRotaciones)
                )
            }

            sb.append(
                "Total efectivo: %.2f\n".format(state.totalEfectivo)
            )

            sb.append("\n")

            sb.append(
                "${state.cantidadCreditos} creditos pendientes: %.2f\n"
                    .format(state.totalPendiente)
            )

            sb.append("\n")
        }

        // 🔹 DEVOLUCIONES
        if (imprimirDevoluciones) {
            sb.append("--- DEVOLUCIONES ---\n")

            state.productosDevueltos.forEach {
                sb.append("${it.nombre}: ${it.cantidad}\n")
            }

            sb.append(
                "Total devoluciones: %.2f\n"
                    .format(state.totalDevoluciones)
            )

            sb.append("\n")
        }

        // 🔹 ROTACIONES
        if (imprimirRotaciones) {

            sb.append("--- ROTACIONES DEL DIA ---\n")

            sb.append(
                "Rotaciones realizadas: ${state.cantidadRotaciones}\n"
            )

            if (state.cantidadRotaciones == 0) {
                sb.append("Sin rotaciones registradas\n")
            } else {

                sb.append("\nPRODUCTOS RECIBIDOS\n")

                if (state.productosRecibidosRotacion.isEmpty()) {
                    sb.append("Sin productos recibidos\n")
                } else {
                    state.productosRecibidosRotacion.forEach {
                        sb.append("${it.nombre}: ${it.cantidad.toInt()} pz.\n")
                    }
                }

                val totalRecibidos =
                    state.productosRecibidosRotacion.sumOf { it.cantidad }

                sb.append(
                    "Total recibido: ${totalRecibidos.toInt()} pz.\n"
                )

                sb.append("\nPRODUCTOS ENTREGADOS\n")

                if (state.productosEntregadosRotacion.isEmpty()) {
                    sb.append("Sin productos entregados\n")
                } else {
                    state.productosEntregadosRotacion.forEach {
                        sb.append("${it.nombre}: ${it.cantidad.toInt()} pz.\n")
                    }
                }

                val totalEntregados =
                    state.productosEntregadosRotacion.sumOf { it.cantidad }

                sb.append(
                    "Total entregado: ${totalEntregados.toInt()} pz.\n"
                )
            }

            sb.append("\n")
        }

        // 🔹 INVENTARIO
        if (imprimirInventario) {
            sb.append("--- INVENTARIO CAMIONETA ---\n")

            if (inventario.isEmpty()) {
                sb.append("Sin inventario\n")
            } else {
                inventario.forEach {
                    sb.append("${it.productoNombre} ${it.presentacion}\n")
                    sb.append(
                        "Ini: ${it.cantidadInicial} | Act: ${it.cantidadActual}\n"
                    )
                }
            }

            sb.append("\n")
        }

        sb.append("------------------------------\n")
        sb.append("\n\n\n")

        return sb.toString()
    }
}
