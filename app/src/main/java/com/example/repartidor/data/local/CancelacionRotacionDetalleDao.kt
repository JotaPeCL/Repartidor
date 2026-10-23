package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.entity.CancelacionRotacionDetalleEntity

@Dao
interface CancelacionRotacionDetalleDao {

    // =========================================================
    // GUARDAR DETALLES
    // =========================================================

    @Insert
    suspend fun insertarDetalles(
        detalles: List<CancelacionRotacionDetalleEntity>
    ): List<Long>

    // =========================================================
    // OBTENER DETALLES POR CANCELACIÓN
    // =========================================================

    @Query("""
        SELECT * FROM cancelacion_rotacion_detalle
        WHERE cancelacionRotacionId = :cancelacionRotacionId
    """)
    suspend fun obtenerPorCancelacion(
        cancelacionRotacionId: Int
    ): List<CancelacionRotacionDetalleEntity>

    // =========================================================
    // OBTENER DETALLES POR UUID
    // =========================================================

    @Query("""
        SELECT * FROM cancelacion_rotacion_detalle
        WHERE cancelacionRotacionUuid = :cancelacionRotacionUuid
    """)
    suspend fun obtenerPorCancelacionUuid(
        cancelacionRotacionUuid: String
    ): List<CancelacionRotacionDetalleEntity>
}