package com.example.repartidor.data.remote

data class PedidoReabastecimientoDetalleDto(
    val id: Int,
    val pedido: Int,
    val producto_variacion: Int,
    val cantidad: Double,
    val updated_at: String
)