package com.example.repartidor.data.remote.request

data class SyncDevolucionesRequest(
    val devoluciones: List<DevolucionRequest>,
    val detalles: List<DevolucionDetalleRequest>,
    val mermas: List<MermaRequest>
)

data class DevolucionRequest(
    val uuid: String,
    val tipo: String,
    val cliente_id: Int?,
    val usuario_id: Int,
    val mini_bodega_id: Int,
    val fecha: String,
    val descripcion: String
)

data class DevolucionDetalleRequest(
    val uuid: String,
    val devolucion_uuid: String,
    val producto_variacion_id: Int,
    val cantidad: Double,
    val precio_unitario: Double
)

data class MermaRequest(
    val uuid: String,
    val mini_bodega_id: Int,
    val producto_variacion_id: Int,
    val cantidad: Double,
    val devolucion_uuid: String?
)