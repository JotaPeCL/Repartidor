package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cliente_dias_visita")
data class ClienteDiasVisitaEntity(
    @PrimaryKey val id: Int,
    val clienteId: Int,
    val diaSemana: String,
    val updatedAt: String
)