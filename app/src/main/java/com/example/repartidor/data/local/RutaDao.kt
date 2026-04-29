package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.RutaEntity

@Dao
interface RutaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rutas: List<RutaEntity>)

    @Query("SELECT id FROM ruta")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM ruta WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM ruta")
    suspend fun getAll(): List<RutaEntity>

    @Query("SELECT * FROM ruta WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): RutaEntity?

    @Query("SELECT * FROM ruta WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun getByUsuarioId(usuarioId: Int): RutaEntity?
}