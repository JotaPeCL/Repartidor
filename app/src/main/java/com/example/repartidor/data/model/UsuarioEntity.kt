package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val telefono: String?,
    val direccion: String?,
    val foto: String?,
    val rolId: Int?, // 👈 FK
    val updatedAt: String
)