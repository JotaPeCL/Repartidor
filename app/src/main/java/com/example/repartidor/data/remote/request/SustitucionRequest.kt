package com.example.repartidor.data.remote.request

data class SustitucionRequest(
    val uuid: String,
    val devolucion_uuid: String,
    val devolucion_detalle_uuid: String,
    val producto_variacion_id: Int,
    val cantidad: Double,
    val precio_unitario: Double
)