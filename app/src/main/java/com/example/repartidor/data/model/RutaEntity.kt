package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ruta")
data class RutaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String?,
    val usuarioId: Int,
    val vehiculoId: Int,
    val estado: Boolean,
    val updatedAt: String
)