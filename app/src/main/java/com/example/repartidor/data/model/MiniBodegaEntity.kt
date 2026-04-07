package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mini_bodega")
data class MiniBodegaEntity(
    @PrimaryKey val id: Int,
    val rutaId: Int,
    val fecha: String,
    val usuarioId: Int,
    val vehiculoId: Int,
    val estado: Boolean,
    val updatedAt: String
)