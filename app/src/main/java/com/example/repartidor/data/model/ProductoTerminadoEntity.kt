package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "producto_terminado")
data class ProductoTerminadoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val categoriaId: Int,
    val imagen: String?,
    val estado: Boolean,
    val updatedAt: String
)