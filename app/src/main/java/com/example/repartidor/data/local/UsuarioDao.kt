package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lista: List<UsuarioEntity>)

    @Query("SELECT id FROM usuario")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM usuario WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM usuario")
    suspend fun getAll(): List<UsuarioEntity>

    @Query("SELECT * FROM usuario WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun getById(id: Int): UsuarioEntity
}