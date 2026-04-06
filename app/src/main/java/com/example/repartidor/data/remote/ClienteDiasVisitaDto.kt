package com.example.repartidor.data.remote

data class ClienteDiasVisitaDto(
    val id: Int,
    val cliente: Int,
    val dia_semana: String,
    val updated_at: String
)