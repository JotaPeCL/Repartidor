package com.example.repartidor.data.model

data class VentaUI(
    val id: Int,
    val nombreCliente: String,
    val nombreNegocio: String?,
    val fecha: Long,
    val total: Double,
    val porcentajeDescuento: Double?
)

data class DetalleVentaUI(
    val nombreCompleto: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)