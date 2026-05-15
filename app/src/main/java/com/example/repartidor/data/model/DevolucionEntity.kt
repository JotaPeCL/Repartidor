package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devoluciones")
data class DevolucionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val tipo: String,
    val clienteId: Int?, // puede ser null
    val usuarioId: Int,
    val miniBodegaId: Int,
    val fecha: String, // viene de la app
    val descripcion: String,
    val sincronizado: Boolean = false,
    val createdAt: String
)