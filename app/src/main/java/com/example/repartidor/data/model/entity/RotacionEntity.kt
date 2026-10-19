package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rotaciones")
data class RotacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val clienteId: Int?,
    val usuarioId: Int,
    val miniBodegaId: Int,
    val fecha: String,
    val descripcion: String,
    // DIFERENCIA ECONÓMICA
    val diferenciaDinero: Double = 0.0,
    val tipoDiferencia: String = "SIN_DIFERENCIA",
    val formaLiquidacion: String = "EFECTIVO",
    val sincronizado: Boolean = false,
    val createdAt: String
)