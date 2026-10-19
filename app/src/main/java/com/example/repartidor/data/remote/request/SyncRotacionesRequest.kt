package com.example.repartidor.data.remote.request

data class RotacionRequest(
    val uuid: String,
    val cliente_id: Int?,
    val usuario_id: Int,
    val mini_bodega_id: Int,
    val fecha: String,
    val descripcion: String,
    val diferencia_dinero: Double,
    val tipo_diferencia: String,
    val forma_liquidacion: String,
    val created_at: String
)

data class RotacionDetalleRequest(
    val uuid: String,
    val rotacion_uuid: String,
    val producto_variacion_id: Int,
    val nombre_producto: String,
    val cantidad: Double,
    val precio_unitario: Double
)

data class RotacionSustitucionRequest(
    val uuid: String,
    val rotacion_uuid: String,
    val rotacion_detalle_uuid: String,
    val producto_variacion_id: Int,
    val nombre_producto: String,
    val cantidad: Double,
    val precio_unitario: Double,
    val created_at: String
)

data class SyncRotacionesRequest(
    val rotaciones: List<RotacionRequest>,
    val detalles: List<RotacionDetalleRequest>,
    val sustituciones: List<RotacionSustitucionRequest>
)