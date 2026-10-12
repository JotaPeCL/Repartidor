package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.entity.RotacionSustitucionEntity

@Dao
interface RotacionSustitucionDao {

    @Insert
    suspend fun insertarSustitucion(
        sustitucion: RotacionSustitucionEntity
    ): Long

    @Insert
    suspend fun insertarSustituciones(
        sustituciones: List<RotacionSustitucionEntity>
    )

    @Query("""
        SELECT * FROM rotacion_sustitucion
        WHERE rotacionId = :rotacionId
    """)
    suspend fun obtenerPorRotacion(
        rotacionId: Int
    ): List<RotacionSustitucionEntity>

    @Query("""
        SELECT * FROM rotacion_sustitucion
        WHERE rotacionUuid IN (:rotacionUuids)
        AND sincronizado = 0
    """)
    suspend fun getPendientesPorRotaciones(
        rotacionUuids: List<String>
    ): List<RotacionSustitucionEntity>

    @Query("""
        UPDATE rotacion_sustitucion
        SET sincronizado = 1
        WHERE uuid = :uuid
    """)
    suspend fun marcarSincronizado(uuid: String)

    @Query("""
        SELECT * FROM rotacion_sustitucion
        WHERE rotacionUuid = :rotacionUuid
    """)
    suspend fun obtenerPorRotacionUuid(
        rotacionUuid: String
    ): List<RotacionSustitucionEntity>
}