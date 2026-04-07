package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.VentaDetalleEntity

@Dao
interface VentaDetalleDao {

    @Insert
    suspend fun insertAll(detalles: List<VentaDetalleEntity>)

    @Query("SELECT * FROM venta_detalle WHERE ventaId = :ventaId")
    suspend fun getByVenta(ventaId: Int): List<VentaDetalleEntity>
}