package com.example.repartidor.data.remote

data class CerrarMiniBodegaRequest(
    val mini_bodega_id: Int,
    val productos: List<MiniBodegaDetalleRequest>
)

data class MiniBodegaDetalleRequest(
    val producto_variacion_id: Int,
    val cantidad_actual: Int
)