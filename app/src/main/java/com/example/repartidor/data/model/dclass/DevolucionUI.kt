package com.example.repartidor.data.model.dclass

data class DevolucionUI(
    val id: Int,
    val clienteId: Int?,
    val nombreCliente: String,
    val nombreNegocio: String?,
    val tipo: String,
    val fecha: Long,
    val total: Double,
    val descripcion: String,
    val diferenciaDinero: Double = 0.0,
    val tipoDiferencia: String = "SIN_DIFERENCIA",
    val formaLiquidacion: String = "EFECTIVO"
)