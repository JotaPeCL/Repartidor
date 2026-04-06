package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.RutaEntity

@Dao
interface RutaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rutas: List<RutaEntity>)

    @Query("SELECT * FROM ruta")
    suspend fun getAll(): List<RutaEntity>
}