package com.example.repartidor.ui.navigation

sealed class Routes(val route: String) {
    object Sync : Routes("sync")
    object Login : Routes("login")
    object Home : Routes("home")
    object Cliente : Routes("cliente")
    object Venta : Routes("venta")
    object Inventario : Routes("inventario")
    object Carrito: Routes("carrito")
}