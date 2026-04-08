package com.example.repartidor.data.model

data class ProductoConStock(
    val id: Int,
    val productoId: Int,
    val precio: Double,
    val stockActual: Double,
    val presentacionNombre: String
)