package com.example.repartidor.data.remote.request

data class CancelacionVentaDetalleRequest(
    val uuid: String,
    val cancelacion_uuid: String,
    val producto_variacion_id: Int,
    val nombre_producto: String,
    val cantidad: Double,
    val precio_unitario: Double
)