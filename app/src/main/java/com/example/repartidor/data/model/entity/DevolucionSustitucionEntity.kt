package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devolucion_sustitucion")
data class DevolucionSustitucionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val devolucionId: Int,
    val devolucionUuid: String,
    val devolucionDetalleId: Int,
    val productoVariacionId: Int,
    val nombreProducto: String,
    val cantidad: Double,
    val precioUnitario: Double = 0.0,
    val createdAt: String,
    val sincronizado: Boolean = false
)