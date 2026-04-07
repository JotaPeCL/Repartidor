package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venta_detalle")
data class VentaDetalleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ventaId: Int,
    val productoVariacionId: Int,
    val cantidad: Double,
    val precioUnitario: Double
)