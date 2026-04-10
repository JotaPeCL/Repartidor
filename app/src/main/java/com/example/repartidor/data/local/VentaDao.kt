package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repartidor.data.model.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.VentaDetalleEntity
import com.example.repartidor.data.model.VentaEntity

@Dao
interface VentaDao {

    @Insert
    suspend fun insert(venta: VentaEntity): Long

    @Insert
    suspend fun insertVenta(venta: VentaEntity): Long

    @Insert
    suspend fun insertDetalles(detalles: List<VentaDetalleEntity>)


    @Query("SELECT * FROM venta")
    suspend fun getAll(): List<VentaEntity>

    @Query(
        """
    SELECT * FROM mini_bodega_detalle 
    WHERE miniBodegaId = :miniBodegaId 
    AND productoVariacionId = :productoId
"""
    )
    suspend fun getProductoStock(
        miniBodegaId: Int,
        productoId: Int
    ): MiniBodegaDetalleEntity?

    @Update
    suspend fun updateMiniBodegaDetalle(detalle: MiniBodegaDetalleEntity)
}