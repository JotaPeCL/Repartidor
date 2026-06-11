package com.example.repartidor.data.model.dclass

data class CarritoItem(
    val productoVariacionId: Int,
    val productoNombre: String,
    val presentacionNombre: String,
    val precio: Double,
    var cantidad: Int
)