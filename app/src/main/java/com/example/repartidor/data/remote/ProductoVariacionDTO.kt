package com.example.repartidor.data.remote

data class ProductoVariacionDTO(
    val id: Int,
    val producto: Int,
    val presentacion: Int,
    val costo: Double,
    val precio: Double,
    val stock: Double,
    val stock_min: Double,
    val codigo_barras: String?,
    val updated_at: String
)