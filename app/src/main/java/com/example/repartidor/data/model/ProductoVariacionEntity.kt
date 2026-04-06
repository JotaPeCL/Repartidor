package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "producto_variacion")
data class ProductoVariacionEntity(
    @PrimaryKey val id: Int,
    val producto: Int,
    val presentacion: Int,
    val costo: Double,
    val precio: Double,
    val stock: Double,
    val stock_min: Double,
    val codigo_barras: String?,
    val updatedAt: String
)