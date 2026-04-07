package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.VentaEntity

@Dao
interface VentaDao {

    @Insert
    suspend fun insert(venta: VentaEntity): Long

    @Query("SELECT * FROM venta")
    suspend fun getAll(): List<VentaEntity>
}