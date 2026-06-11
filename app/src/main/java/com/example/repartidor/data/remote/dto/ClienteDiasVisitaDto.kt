package com.example.repartidor.data.remote.dto

data class ClienteDiasVisitaDto(
    val id: Int,
    val cliente: Int,
    val dia_semana: String,
    val updated_at: String
)