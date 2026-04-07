package com.example.repartidor.data.remote

data class MiniBodegaDetalleDto(
    val id: Int,
    val mini_bodega: Int,
    val producto_variacion: Int,
    val cantidad_inicial: Double,
    val cantidad_actual: Double
)