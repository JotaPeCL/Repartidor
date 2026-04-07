package com.example.repartidor.data.model

data class InventarioItem(
    val productoNombre: String,
    val presentacion: String,
    val cantidadActual: Double,
    val cantidadInicial: Double
)