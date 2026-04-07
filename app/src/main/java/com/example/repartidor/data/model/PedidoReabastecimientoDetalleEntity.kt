package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedido_reabastecimiento_detalle")
data class PedidoReabastecimientoDetalleEntity(
    @PrimaryKey val id: Int,
    val pedidoId: Int,
    val productoVariacionId: Int,
    val cantidad: Double,
    val updatedAt: String
)