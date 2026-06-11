package com.example.repartidor.data.remote.dto

data class PedidoReabastecimientoDto(
    val id: Int,
    val ruta: Int,
    val fecha: String,
    val estado: Boolean,
    val updated_at: String,
    val usuario:Int
)