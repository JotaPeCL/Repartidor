package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presentacion_producto")
data class PresentacionProductoTerminadoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String?,
    val imagen: String?,
    val estado: Boolean,
    val updatedAt: String
)