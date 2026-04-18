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
}