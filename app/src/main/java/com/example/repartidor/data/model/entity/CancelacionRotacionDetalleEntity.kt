package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancelacion_rotacion_detalle")
data class CancelacionRotacionDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val cancelacionRotacionId: Int,
    val cancelacionRotacionUuid: String,
    val productoVariacionId: Int,
    val nombreProducto: String,
    val cantidad: Double,
    val precioUnitario: Double = 0.0,
    val tipoMovimiento: String
)