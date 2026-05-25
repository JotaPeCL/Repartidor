package com.example.repartidor.data.remote

data class SyncVentasRequest(
    val ventas: List<VentaRequest>,
    val detalles: List<VentaDetalleRequest>
)

data class VentaRequest(
    val uuid: String,
    val usuario_id: Int,
    val cliente_id: Int?,
    val total: Double,
    val tipo_venta: String,
    val fecha: String,
)

data class VentaDetalleRequest(
    val uuid: String,
    val venta_uuid: String,
    val producto_variacion_id: Int,
    val cantidad: Double,
    val precio_unitario: Double,
    val nombre_producto: String
)