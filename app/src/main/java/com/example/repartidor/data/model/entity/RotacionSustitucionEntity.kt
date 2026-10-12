package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rotacion_sustitucion")
data class RotacionSustitucionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val rotacionId: Int,
    val rotacionUuid: String,
    val rotacionDetalleId: Int,
    val productoVariacionId: Int,
    val nombreProducto: String,
    val cantidad: Double,
    val precioUnitario: Double = 0.0,
    val createdAt: String,
    val sincronizado: Boolean = false
)