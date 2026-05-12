package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repartidor.data.model.DevolucionDetalleEntity
import com.example.repartidor.data.model.DevolucionEntity

@Dao
interface DevolucionDao {

    @Insert
    suspend fun insert(devolucion: DevolucionEntity): Long

    @Query("SELECT * FROM devoluciones WHERE sincronizado = 0")
    suspend fun getPendientes(): List<DevolucionEntity>

    @Update
    suspend fun update(devolucion: DevolucionEntity)

    @Insert
    suspend fun insertarDevolucion(devolucion: DevolucionEntity): Long

    @Insert
    suspend fun insertarDetalles(detalles: List<DevolucionDetalleEntity>)
}