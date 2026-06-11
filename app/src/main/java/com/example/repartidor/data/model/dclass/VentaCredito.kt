package com.example.repartidor.data.model.dclass

data class VentaCredito(
    val id: Int,
    val uuid: String,
    val clienteId: Int,
    val total: Double,
    val saldoPendiente: Double,
    val estadoPago: String,

    val nombre: String?,
    val nombreNegocio: String?,

    val totalAbonado: Double,
    val saldoCalculado: Double
)