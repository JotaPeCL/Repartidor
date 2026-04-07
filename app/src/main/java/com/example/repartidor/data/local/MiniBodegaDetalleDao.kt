package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.MiniBodegaDetalleEntity

@Dao
interface MiniBodegaDetalleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(detalles: List<MiniBodegaDetalleEntity>)

    @Query("SELECT * FROM mini_bodega_detalle WHERE miniBodegaId = :miniBodegaId")
    suspend fun getByMiniBodega(miniBodegaId: Int): List<MiniBodegaDetalleEntity>
}