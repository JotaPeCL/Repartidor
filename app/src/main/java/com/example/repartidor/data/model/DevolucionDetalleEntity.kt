package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devolucion_detalle")
data class DevolucionDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val devolucionId: Int,
    val productoVariacionId: Int,
    val nombreProducto: String,
    val cantidad: Double,
    val precioUnitario: Double = 0.0
)