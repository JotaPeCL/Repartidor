package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.PedidoReabastecimientoEntity

@Dao
interface PedidoReabastecimientoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pedidos: List<PedidoReabastecimientoEntity>)

    @Query("SELECT id FROM pedido_reabastecimiento")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM pedido_reabastecimiento WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM pedido_reabastecimiento")
    suspend fun getAll(): List<PedidoReabastecimientoEntity>
}