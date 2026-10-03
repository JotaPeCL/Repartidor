package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancelacion_venta")
data class CancelacionVentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val ventaUuid: String,
    val usuarioId: Int,
    val miniBodegaId: Int,
    val fecha: String,
    val total: Double,
    val motivo: String = "",
    val sincronizado: Boolean = false,
    val createdAt: String
)