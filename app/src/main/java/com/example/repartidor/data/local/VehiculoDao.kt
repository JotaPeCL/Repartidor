package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.VehiculoEntity

@Dao
interface VehiculoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehiculos: List<VehiculoEntity>)

    @Query("SELECT * FROM vehiculo")
    suspend fun getAll(): List<VehiculoEntity>

    @Query("SELECT * FROM vehiculo WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): VehiculoEntity?
}