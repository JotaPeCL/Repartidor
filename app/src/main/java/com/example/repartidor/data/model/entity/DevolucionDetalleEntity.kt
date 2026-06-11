package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devolucion_detalle")
data class DevolucionDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val devolucionId: Int,
    val devolucionUuid: String,
    val productoVariacionId: Int,
    val nombreProducto: String,
    val cantidad: Double,
    val precioUnitario: Double = 0.0
)