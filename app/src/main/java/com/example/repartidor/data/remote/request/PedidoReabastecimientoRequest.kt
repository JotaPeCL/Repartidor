package com.example.repartidor.data.remote.request

data class PedidoReabastecimientoRequest(
    val uuid: String,
    val ruta_id: Int,
    val productos: List<PedidoDetalleRequest>
)

data class PedidoDetalleRequest(
    val producto_variacion_id: Int,
    val cantidad: Int
)