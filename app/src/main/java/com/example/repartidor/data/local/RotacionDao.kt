package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repartidor.data.model.entity.RotacionEntity

@Dao
interface RotacionDao {

    @Insert
    suspend fun insert(rotacion: RotacionEntity): Long

    @Update
    suspend fun update(rotacion: RotacionEntity)

    @Query("SELECT * FROM rotaciones WHERE sincronizado = 0")
    suspend fun getPendientes(): List<RotacionEntity>

    @Query("UPDATE rotaciones SET sincronizado = 1 WHERE uuid = :uuid")
    suspend fun marcarSincronizado(uuid: String)

    @Query("SELECT * FROM rotaciones WHERE id = :id")
    suspend fun getById(id: Int): RotacionEntity?

    @Query("""
    SELECT * FROM rotaciones
    WHERE CAST(fecha AS INTEGER) >= :inicio
    AND CAST(fecha AS INTEGER) < :fin
    AND usuarioId = :usuarioId
    ORDER BY CAST(fecha AS INTEGER) DESC
""")
    suspend fun getRotacionesDelDia(
        inicio: Long,
        fin: Long,
        usuarioId: Int
    ): List<RotacionEntity>

    @Query("""
    SELECT * FROM rotaciones
    WHERE usuarioId = :usuarioId
    AND fecha >= :inicio
    AND fecha < :fin
    ORDER BY fecha DESC
""")
    suspend fun getRotacionesParaResumen(
        inicio: String,
        fin: String,
        usuarioId: Int
    ): List<RotacionEntity>
}