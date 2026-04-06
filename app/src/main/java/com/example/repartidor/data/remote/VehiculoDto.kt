package com.example.repartidor.data.remote

data class VehiculoDto(
    val id: Int,
    val marca: String,
    val color: String,
    val placa: String,
    val kilometraje: Double,
    val ultimo_servicio: String,
    val observaciones: String?,
    val imagen: String?,
    val estado: Boolean,
    val updated_at: String
)