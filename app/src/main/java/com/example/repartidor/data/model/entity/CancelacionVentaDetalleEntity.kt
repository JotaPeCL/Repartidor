package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancelacion_venta_detalle")
data class CancelacionVentaDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val cancelacionVentaId: Int,
    val cancelacionVentaUuid: String,
    val productoVariacionId: Int,
    val nombreProducto: String,
    val cantidad: Double,
    val precioUnitario: Double
)