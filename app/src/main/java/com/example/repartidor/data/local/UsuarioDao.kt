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

    @Query("SELECT * FROM usuario")
    suspend fun getAll(): List<UsuarioEntity>

    @Query("SELECT * FROM usuario WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UsuarioEntity?
}