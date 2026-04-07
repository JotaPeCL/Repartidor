package com.example.repartidor.data.remote

data class MiniBodegaDto(
    val id: Int,
    val ruta: Int,
    val fecha: String,
    val usuario: Int,
    val vehiculo: Int,
    val estado: Boolean,
    val updated_at: String
)