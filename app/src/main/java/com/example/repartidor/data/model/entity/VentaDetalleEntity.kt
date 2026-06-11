package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venta_detalle")
data class VentaDetalleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String,
    val ventaId: Int,
    val ventaUuid: String,
    val productoVariacionId: Int,
    val nombreProducto: String,
    val cantidad: Double,
    val precioUnitario: Double
)