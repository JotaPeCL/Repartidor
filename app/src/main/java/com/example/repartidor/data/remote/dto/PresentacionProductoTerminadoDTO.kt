package com.example.repartidor.data.remote.dto

data class PresentacionProductoTerminadoDTO(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val imagen: String?,
    val estado: Boolean,
    val updated_at: String
)