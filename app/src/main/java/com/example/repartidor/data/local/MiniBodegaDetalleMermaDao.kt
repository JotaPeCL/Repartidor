package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.MiniBodegaDetalleMermaEntity

@Dao
interface MiniBodegaDetalleMermaDao {

    @Insert
    suspend fun insert(merma: MiniBodegaDetalleMermaEntity)

    @Query("SELECT * FROM mini_bodega_detalle_merma")
    suspend fun getAll(): List<MiniBodegaDetalleMermaEntity>

    @Insert
    suspend fun insertarMerma(merma: MiniBodegaDetalleMermaEntity)
}