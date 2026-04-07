package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.PedidoReabastecimientoDetalleEntity

@Dao
interface PedidoReabastecimientoDetalleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(detalles: List<PedidoReabastecimientoDetalleEntity>)

    @Query("SELECT * FROM pedido_reabastecimiento_detalle WHERE pedidoId = :pedidoId")
    suspend fun getByPedido(pedidoId: Int): List<PedidoReabastecimientoDetalleEntity>
}