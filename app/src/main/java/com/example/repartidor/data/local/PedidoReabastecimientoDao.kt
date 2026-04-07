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

    @Query("SELECT * FROM pedido_reabastecimiento")
    suspend fun getAll(): List<PedidoReabastecimientoEntity>
}