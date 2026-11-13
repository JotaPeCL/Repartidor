
package com.example.repartidor.utils

import com.example.repartidor.data.model.dclass.InventarioItem
import com.example.repartidor.data.model.dclass.ResumenDiaState
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.util.Locale

object ResumenTicketBuilder {

    private val ORDEN_PRESENTACIONES = listOf(
        "Minis", "Chico", "Mediano", "Grande", "250g", "500g",
        "1kg", "Kiosko", "Grande 150g", "Grande 140g", "Pieza"
    )

    private val ORDEN_PRODUCTOS = listOf(
        "PISTACHE", "ENCHILADO", "JAPONES", "JAPONES FUEGO", "MACHO",
        "HORNEADO", "CASCARITA", "SURTIDO", "HABA PELADA", "HABA CON CASCARA",
        "GARBANZA", "GARAPIÑADO", "GOMITA", "LUNETA", "HUEVITO CHOCOLATE",
        "GALLETA MARINA", "CIRUELA", "PASAS", "SEMILLA DE CALABAZA",
        "SEMILLA DE GIRASOL", "NUEZ", "ALMENDRA", "GRANOLA", "CHILE DE ARBOL",
        "CHILE PULLA", "CHILE CASCABEL", "CAMARON", "CAMARON MOLIDO",
        "CHARAL NATURAL", "CHARAL ADOBADO", "CECINA NATURAL", "CECINA ADOBADA",
        "CECINA DE LIMON", "CECINA CHILTEPIN", "CECINA CHIPOTLE",
        "CECINA HABANERO", "CECINA ARTESANAL", "PAPA FRITA",
        "CHURRO DE MASA", "CHURRO DELGADO", "CHICHARRON", "CHICHARRON CRAC",
        "PLATANO", "TOSTADA", "MIEL", "TIRA DE CC MINI", "TIRA DE DULCE",
        "MIX VERDE", "MIX ROJO"
    )

    private fun String.normalizar(): String =
        Normalizer.normalize(trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .uppercase(Locale.ROOT)

    private val INDICE_PRODUCTOS = ORDEN_PRODUCTOS.mapIndexed { i, nombre ->
        nombre.normalizar() to i
    }.toMap()

    private val INDICE_PRESENTACIONES = ORDEN_PRESENTACIONES.mapIndexed { i, nombre ->
        nombre.normalizar() to i
    }.toMap()

    private fun dinero(valor: BigDecimal): String =
        "$" + valor.setScale(2, RoundingMode.HALF_UP).toPlainString()

    private fun cantidad(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()

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

        // PRODUCTOS VENDIDOS
        if (imprimirProductos) {
            sb.append("--- PRODUCTOS VENDIDOS ---\n")
            state.productosVendidos.forEach {
                sb.append("${it.nombre}: ${it.cantidad}\n")
            }
            sb.append("\n")
        }

        // DINERO
        if (imprimirDinero) {
            sb.append("--- DINERO ---\n")
            sb.append("Ventas totales: %.2f\n".format(state.dineroTotalVentas))
            sb.append("Efectivo en ventas: %.2f\n".format(state.efectivoVentas))
            sb.append("Abonos: %.2f\n".format(state.totalAbonos))

            if (state.cantidadRotaciones > 0) {
                sb.append("Recibido rotaciones: +%.2f\n".format(state.totalRotacionesRecibido))
                sb.append("Entregado rotaciones: -%.2f\n".format(state.totalRotacionesEntregado))
                sb.append("Balance rotaciones: %+.2f\n".format(state.balanceRotaciones))
            }

            sb.append("Total efectivo: %.2f\n".format(state.totalEfectivo))
            sb.append("\n")
            sb.append("${state.cantidadCreditos} creditos pendientes: %.2f\n".format(state.totalPendiente))
            sb.append("\n")
        }

        // DEVOLUCIONES
        if (imprimirDevoluciones) {
            sb.append("--- DEVOLUCIONES ---\n")
            state.productosDevueltos.forEach {
                sb.append("${it.nombre}: ${it.cantidad}\n")
            }
            sb.append("Total devoluciones: %.2f\n".format(state.totalDevoluciones))
            sb.append("\n")
        }

        // ROTACIONES
        if (imprimirRotaciones) {
            sb.append("--- ROTACIONES DEL DIA ---\n")
            sb.append("Rotaciones realizadas: ${state.cantidadRotaciones}\n")

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

                val totalRecibidos = state.productosRecibidosRotacion.sumOf { it.cantidad }
                sb.append("Total recibido: ${totalRecibidos.toInt()} pz.\n")

                sb.append("\nPRODUCTOS ENTREGADOS\n")

                if (state.productosEntregadosRotacion.isEmpty()) {
                    sb.append("Sin productos entregados\n")
                } else {
                    state.productosEntregadosRotacion.forEach {
                        sb.append("${it.nombre}: ${it.cantidad.toInt()} pz.\n")
                    }
                }

                val totalEntregados = state.productosEntregadosRotacion.sumOf { it.cantidad }
                sb.append("Total entregado: ${totalEntregados.toInt()} pz.\n")
            }
            sb.append("\n")
        }

        // INVENTARIO AGRUPADO POR PRECIO
        if (imprimirInventario) {
            sb.append("--- INVENTARIO CAMIONETA ---\n")

            if (inventario.isEmpty()) {
                sb.append("Sin inventario\n")
            } else {
                val grupos = inventario.groupBy {
                    BigDecimal.valueOf(it.precioUnitario).setScale(2, RoundingMode.HALF_UP)
                }.toSortedMap()

                var totalInventario = BigDecimal.ZERO

                grupos.forEach { (precio, productos) ->
                    sb.append("\n=== PRECIO ${dinero(precio)} ===\n")

                    val productosOrdenados = productos.sortedWith(
                        compareBy(
                            { INDICE_PRODUCTOS[it.productoNombre.normalizar()] ?: Int.MAX_VALUE },
                            { if (it.productoNombre.normalizar() in INDICE_PRODUCTOS) "" else it.productoNombre.normalizar() },
                            { INDICE_PRESENTACIONES[it.presentacion.normalizar()] ?: Int.MAX_VALUE },
                            { it.presentacion.normalizar() }
                        )
                    )

                    var subtotalGrupo = BigDecimal.ZERO

                    productosOrdenados.forEach { item ->
                        val importe = precio.multiply(BigDecimal.valueOf(item.cantidadActual))
                        subtotalGrupo = subtotalGrupo.add(importe)

                        sb.append("${item.productoNombre} ${item.presentacion}\n")
                        sb.append(" ${cantidad(item.cantidadActual)} pz. = ${dinero(importe)}\n")
                    }

                    totalInventario = totalInventario.add(subtotalGrupo)

                    sb.append("------------------------------\n")
                    sb.append("SUBTOTAL: ${dinero(subtotalGrupo)}\n")
                }

                sb.append("==============================\n")
                sb.append("TOTAL INVENTARIO\n")
                sb.append("${dinero(totalInventario)}\n")
                sb.append("==============================\n")
            }

            sb.append("\n")
        }

        sb.append("------------------------------\n")
        sb.append("\n\n\n")

        return sb.toString()
    }
}
