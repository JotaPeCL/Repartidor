package com.example.repartidor.data.model.dclass

data class SustitucionRotacionSeleccionada(
    val productoVariacionId: Int,
    val nombreProducto: String,
    val presentacionNombre: String,
    val precio: Double,
    val cantidad: Int
)