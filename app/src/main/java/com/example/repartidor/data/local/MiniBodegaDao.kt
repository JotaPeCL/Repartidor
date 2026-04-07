package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.MiniBodegaEntity

@Dao
interface MiniBodegaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(miniBodegas: List<MiniBodegaEntity>)

    @Query("SELECT * FROM mini_bodega")
    suspend fun getAll(): List<MiniBodegaEntity>
}