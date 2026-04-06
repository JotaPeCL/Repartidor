package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cliente")
data class ClienteEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val nombreNegocio: String,
    val giro: String?,
    val tipoExhibidor: String?,
    val direccion: String,
    val localidad: String?,
    val colonia: String?,
    val telefono: String?,
    val credito: Double?,
    val imagen: String?,
    val observaciones: String?,
    val rutaId: Int?,
    val estado: Boolean,
    val updatedAt: String
)