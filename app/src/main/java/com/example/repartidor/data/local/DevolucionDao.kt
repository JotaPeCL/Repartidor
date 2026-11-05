package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repartidor.data.model.entity.DevolucionDetalleEntity
import com.example.repartidor.data.model.entity.DevolucionEntity

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
    suspend fun insertarDetalles(detalles: List<DevolucionDetalleEntity>): List<Long>

    @Query("""
    SELECT * FROM devoluciones 
    WHERE sincronizado = 0 
    AND fecha BETWEEN :inicio AND :fin
""")
    suspend fun getDevolucionesNoSincronizadas(inicio: Long, fin: Long): List<DevolucionEntity>

    @Query("""
        UPDATE devoluciones 
        SET sincronizado = 1 
        WHERE uuid = :uuid
    """)
    suspend fun marcarSincronizado(uuid: String)

    @Query("""
SELECT d.id FROM devoluciones d
WHERE d.sincronizado = 1
AND CAST(d.fecha AS INTEGER) < :fechaLimite
AND NOT EXISTS (
    SELECT 1 FROM devolucion_sustitucion s
    WHERE s.devolucionId = d.id AND s.sincronizado = 0
)
AND NOT EXISTS (
    SELECT 1 FROM mini_bodega_detalle_merma m
    WHERE m.devolucionId = d.id AND m.sincronizado = 0
)
""")
    suspend fun getDevolucionesParaEliminar(fechaLimite: Long): List<Int>

    @Query("DELETE FROM devoluciones WHERE id IN (:ids)")
    suspend fun deleteDevolucionesByIds(ids: List<Int>)


    @Query("""
    SELECT * FROM devoluciones
    WHERE id = :id
""")
    suspend fun getById(id: Int): DevolucionEntity?

    @Query("""
    DELETE FROM devoluciones
    WHERE id = :id
""")
    suspend fun deleteById(id: Int)

    @Query("""
    SELECT * FROM devoluciones
    WHERE fecha BETWEEN :inicio AND :fin
    AND usuarioId = :usuarioId
    ORDER BY fecha DESC
""")
    suspend fun getDevolucionesDelDia(
        inicio: Long,
        fin: Long,
        usuarioId: Int
    ): List<DevolucionEntity>
}