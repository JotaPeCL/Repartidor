package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.entity.RotacionDetalleEntity

@Dao
interface RotacionDetalleDao {

    @Insert
    suspend fun insertarDetalle(
        detalle: RotacionDetalleEntity
    ): Long

    @Insert
    suspend fun insertarDetalles(
        detalles: List<RotacionDetalleEntity>
    ): List<Long>

    @Query("""
        SELECT * FROM rotacion_detalle
        WHERE rotacionId = :rotacionId
    """)
    suspend fun obtenerPorRotacion(
        rotacionId: Int
    ): List<RotacionDetalleEntity>

    @Query("""
        SELECT * FROM rotacion_detalle
        WHERE rotacionUuid = :rotacionUuid
    """)
    suspend fun obtenerPorRotacionUuid(
        rotacionUuid: String
    ): List<RotacionDetalleEntity>
}