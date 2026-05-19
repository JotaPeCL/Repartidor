package com.example.repartidor.data.model

data class VentaInfo(
    val id: Int,
    val fecha: String,
    val tipoVenta: String,
    val total: Double,

    val clienteNombre: String,
    val clienteNegocio: String,
    val porcentajeDescuento: Double,

    val totalAbonado: Double,
    val saldoPendiente: Double
)

data class ProductoVenta(
    val nombre: String,
    val cantidad: Double,
    val precioUnitario: Double
)

data class VentaDetalleCompleta(
    val info: VentaInfo,
    val productos: List<ProductoVenta>
)