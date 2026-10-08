package com.example.repartidor.data.remote.request

data class CancelacionDevolucionRequest(
    val uuid: String,
    val devolucion_uuid: String,
    val usuario_id: Int,
    val mini_bodega_id: Int,
    val fecha: String,
    val total: Double,
    val motivo: String
)