package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.entity.DevolucionSustitucionEntity

@Dao
interface DevolucionSustitucionDao {

    @Insert
    suspend fun insertarSustitucion(
        sustitucion: DevolucionSustitucionEntity
    ): Long

    @Insert
    suspend fun insertarSustituciones(
        sustituciones: List<DevolucionSustitucionEntity>
    )

    @Query("""
        SELECT * FROM devolucion_sustitucion
        WHERE devolucionId = :devolucionId
    """)
    suspend fun obtenerPorDevolucion(
        devolucionId: Int
    ): List<DevolucionSustitucionEntity>

    @Query("""
    SELECT * FROM devolucion_sustitucion
    WHERE devolucionUuid IN (:devolucionUuids)
    AND sincronizado = 0
""")
    suspend fun getSustitucionesByDevolucionUuids(
        devolucionUuids: List<String>
    ): List<DevolucionSustitucionEntity>


    @Query("""
        UPDATE devolucion_sustitucion
        SET sincronizado = 1
        WHERE uuid = :uuid
    """)
    suspend fun marcarSincronizado(
        uuid: String
    )
}
