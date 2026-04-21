package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.ClienteEntity

@Dao
interface ClienteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clientes: List<ClienteEntity>)

    @Query("SELECT * FROM cliente")
    suspend fun getAll(): List<ClienteEntity>

    @Query("SELECT * FROM cliente WHERE id = :id LIMIT 1")
    suspend fun obtenerClientePorId(id: Int): ClienteEntity?

    @Query("SELECT * FROM cliente WHERE id = :id LIMIT 1")
    suspend fun getClienteById(id: Int): ClienteEntity?
}