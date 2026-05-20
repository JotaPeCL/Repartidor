package com.example.repartidor.ui.navigation

sealed class Routes(val route: String) {
    object Sync : Routes("sync")
    object Login : Routes("login")
    object Home : Routes("home")
    object Cliente : Routes("cliente")
    object QrScanner : Routes("qr_scanner")
    object Venta : Routes("venta?clienteId={clienteId}")
    object Inventario : Routes("inventario")
    object Carrito: Routes("carrito")
    object Reabastecimiento : Routes("reabastecimiento")
    object PedidoReabastecimiento : Routes("pedido_reabastecimiento")
    object Bluetooth : Routes("bluetooth")
    object VentasDia : Routes("ventas_dia")
    object Devolucion : Routes("devolucion")
    object CarritoDevolucion : Routes("carrito_devolucion")
    object Abonos : Routes("abonos")
    object AbonoForm : Routes("abono_form/{ventaId}") {
        fun createRoute(ventaId: Int) = "abono_form/$ventaId"
    }
    object ResumenDia: Routes("resumen_dia")
}