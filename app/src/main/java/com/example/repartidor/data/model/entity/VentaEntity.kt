package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venta")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String,
    val clienteId: Int?,
    val usuarioId: Int,
    val fecha: String,
    val total: Double,
    val tipoVenta: String,
    val estadoPago: String,
    val saldoPendiente: Double,
    val fechaVencimiento: String?,
    val sincronizado: Boolean = false
)