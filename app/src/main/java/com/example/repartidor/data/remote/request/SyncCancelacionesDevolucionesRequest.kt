package com.example.repartidor.data.remote.request

data class SyncCancelacionesDevolucionesRequest(
    val cancelaciones: List<CancelacionDevolucionRequest>,
    val detalles: List<CancelacionDevolucionDetalleRequest>,
    val sustituciones: List<CancelacionDevolucionSustitucionRequest>
)