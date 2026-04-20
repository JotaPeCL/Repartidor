package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mini_bodega_detalle")
data class MiniBodegaDetalleEntity(
    @PrimaryKey val id: Int,
    val miniBodegaId: Int,
    val productoVariacionId: Int,
    val cantidadInicial: Double,
    val cantidadActual: Double,
    val updatedAt: String
)