package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venta")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clienteId: Int,
    val fecha: String,
    val total: Double,
    val sincronizado: Boolean = false
)