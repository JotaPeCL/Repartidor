package com.example.repartidor.data.model.dclass

data class ProductoSustitucion(
    val id: Int,
    val productoId: Int,
    val nombreProducto: String,
    val presentacionNombre: String,
    val precio: Double,
    val stockActual: Double
)