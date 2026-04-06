package com.example.repartidor.data.remote

data class RutaDto(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val usuario: Int,
    val vehiculo: Int,
    val estado: Boolean,
    val updated_at: String
)