package com.example.repartidor.data.remote.request

data class SyncCancelacionesRequest(
    val cancelaciones: List<CancelacionVentaRequest>,
    val detalles: List<CancelacionVentaDetalleRequest>
)