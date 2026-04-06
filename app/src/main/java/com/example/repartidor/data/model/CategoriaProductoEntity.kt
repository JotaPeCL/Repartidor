package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categoria_producto")
data class CategoriaProductoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String?,
    val imagen: String?,
    val estado: Boolean,
    val updatedAt: String
)