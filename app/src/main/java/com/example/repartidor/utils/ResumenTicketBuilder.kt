package com.example.repartidor.utils

import com.example.repartidor.data.model.InventarioItem
import com.example.repartidor.data.model.ResumenDiaState

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
        imprimirDevoluciones: Boolean
    ): String {

        val sb = StringBuilder()

        sb.append("\n")
        sb.append("===== RESUMEN DEL DIA =====\n")
        sb.append("Usuario: $nombreUsuario\n")
        sb.append("Ruta: $ruta\n")
        sb.append("Fecha: $fecha\n")

        //sb.append("\n--------------------------\n\n")

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

            sb.append("Efectivo en ventas: %.2f\n".format(state.efectivoVentas))
            sb.append("Abonos: %.2f\n".format(state.totalAbonos))
            sb.append("Total efectivo: %.2f\n".format(state.totalEfectivo))

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
                    sb.append("Ini: ${it.cantidadInicial} | Act: ${it.cantidadActual}\n")
                }
            }

            sb.append("\n")
        }

        sb.append("------------------------------\n")
        sb.append("\n\n\n")

        return sb.toString()
    }
}