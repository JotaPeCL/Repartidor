package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancelacion_devolucion_sustitucion")
data class CancelacionDevolucionSustitucionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val cancelacionDevolucionId: Int,
    val cancelacionDevolucionUuid: String,
    val productoVariacionId: Int,
    val nombreProducto: String,
    val cantidad: Double,
    val precioUnitario: Double
)