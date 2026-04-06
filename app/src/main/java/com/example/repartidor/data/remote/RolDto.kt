package com.example.repartidor.data.remote

data class RolDto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val estado: Boolean,
    val updated_at: String
)