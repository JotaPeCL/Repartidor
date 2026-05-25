package com.example.repartidor.data.remote

data class SyncAbonosRequest(
    val abonos: List<AbonoRequest>
)

data class AbonoRequest(
    val uuid: String,
    val venta_uuid: String,
    val usuario_id: Int,
    val monto: Double,
    val fecha: String
)