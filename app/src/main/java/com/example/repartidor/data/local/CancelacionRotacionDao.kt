package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.entity.CancelacionRotacionEntity

@Dao
interface CancelacionRotacionDao {

    // =========================================================
    // GUARDAR CANCELACIÓN
    // =========================================================

    @Insert
    suspend fun insertar(
        cancelacion: CancelacionRotacionEntity
    ): Long

    // =========================================================
    // OBTENER CANCELACIONES PENDIENTES DE SINCRONIZAR
    // =========================================================

    @Query("""
        SELECT * FROM cancelacion_rotacion
        WHERE sincronizado = 0
        ORDER BY id ASC
    """)
    suspend fun getPendientes(): List<CancelacionRotacionEntity>

    // =========================================================
    // MARCAR COMO SINCRONIZADA
    // =========================================================

    @Query("""
        UPDATE cancelacion_rotacion
        SET sincronizado = 1
        WHERE uuid = :uuid
    """)
    suspend fun marcarSincronizado(uuid: String)

    // =========================================================
    // OBTENER CANCELACIÓN POR UUID
    // =========================================================

    @Query("""
        SELECT * FROM cancelacion_rotacion
        WHERE uuid = :uuid
        LIMIT 1
    """)
    suspend fun getByUuid(uuid: String): CancelacionRotacionEntity?

    // =========================================================
    // VERIFICAR SI UNA ROTACIÓN YA FUE CANCELADA
    // =========================================================

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM cancelacion_rotacion
            WHERE rotacionUuid = :rotacionUuid
        )
    """)
    suspend fun existeCancelacion(
        rotacionUuid: String
    ): Boolean
}