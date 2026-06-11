package com.example.repartidor.data.remote.dto

data class MiniBodegaDetalleDto(
    val id: Int,
    val mini_bodega: Int,
    val producto_variacion: Int,
    val cantidad_inicial: Double,
    val cantidad_actual: Double,
    val updated_at: String
)