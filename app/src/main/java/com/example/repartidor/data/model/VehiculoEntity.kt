package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehiculo")
data class VehiculoEntity(
    @PrimaryKey val id: Int,
    val marca: String,
    val color: String,
    val placa: String,
    val kilometraje: Double,
    val ultimoServicio: String,
    val observaciones: String?,
    val imagen: String?,
    val estado: Boolean,
    val updatedAt: String
)