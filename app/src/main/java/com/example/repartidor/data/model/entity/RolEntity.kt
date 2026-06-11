package com.example.repartidor.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class RolEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String,
    val estado: Boolean,
    val updated_at: String
)