package com.example.repartidor.data.remote

data class ProductoTerminadoDTO(
    val id: Int,
    val nombre: String,
    val categoria_producto: Int,
    val imagen: String?,
    val estado: Boolean,
    val updated_at: String
)