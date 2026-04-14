package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedido_reabastecimiento")
data class PedidoReabastecimientoEntity(
    @PrimaryKey val id: Int,
    val rutaId: Int,
    val fecha: String,
    val estado: Boolean,
    val updatedAt: String,
    val usuario:Int
)