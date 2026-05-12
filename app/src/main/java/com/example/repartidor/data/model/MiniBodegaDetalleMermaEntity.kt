package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mini_bodega_detalle_merma")
data class MiniBodegaDetalleMermaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val miniBodegaId: Int,
    val productoVariacionId: Int,
    val cantidad: Double,
    val devolucionId: Int?,
    val createdAt: String
)