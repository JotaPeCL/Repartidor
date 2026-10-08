package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancelacion_devolucion")
data class CancelacionDevolucionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val devolucionUuid: String,
    val usuarioId: Int,
    val miniBodegaId: Int,
    val fecha: String,
    val total: Double,
    val motivo: String = "",
    val sincronizado: Boolean = false,
    val createdAt: String
)
