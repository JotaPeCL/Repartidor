package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancelacion_rotacion")
data class CancelacionRotacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val rotacionUuid: String,
    val usuarioId: Int,
    val miniBodegaId: Int,
    val fecha: String,
    val descripcion: String,
    val diferenciaDinero: Double = 0.0,
    val tipoDiferencia: String = "SIN_DIFERENCIA",
    val formaLiquidacion: String = "EFECTIVO",
    val motivo: String,
    val sincronizado: Boolean = false,
    val createdAt: String
)