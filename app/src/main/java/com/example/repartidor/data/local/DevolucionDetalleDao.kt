package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.DevolucionDetalleEntity

@Dao
interface DevolucionDetalleDao {

    @Insert
    suspend fun insertAll(detalles: List<DevolucionDetalleEntity>)

    @Query("SELECT * FROM devolucion_detalle WHERE devolucionId = :id")
    suspend fun getByDevolucion(id: Int): List<DevolucionDetalleEntity>
}