package com.example.repartidor.data.remote.request

data class CancelacionRotacionRequest(
    val uuid: String,
    val rotacion_uuid: String,
    val usuario_id: Int,
    val mini_bodega_id: Int,
    val fecha: String,
    val descripcion: String,
    val diferencia_dinero: Double,
    val tipo_diferencia: String,
    val forma_liquidacion: String,
    val motivo: String,
    val created_at: String
)

data class CancelacionRotacionDetalleRequest(
    val uuid: String,
    val cancelacion_uuid: String,
    val producto_variacion_id: Int,
    val nombre_producto: String,
    val cantidad: Double,
    val precio_unitario: Double,
    val tipo_movimiento: String
)

data class SyncCancelacionesRotacionesRequest(
    val cancelaciones: List<CancelacionRotacionRequest>,
    val detalles: List<CancelacionRotacionDetalleRequest>
)