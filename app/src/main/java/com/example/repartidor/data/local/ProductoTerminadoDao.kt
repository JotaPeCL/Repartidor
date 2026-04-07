package com.example.repartidor.data.local

import androidx.room.*
import com.example.repartidor.data.model.ProductoTerminadoEntity

@Dao
interface ProductoTerminadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lista: List<ProductoTerminadoEntity>)

    @Query("SELECT * FROM producto_terminado")
    suspend fun getAll(): List<ProductoTerminadoEntity>

    @Query("DELETE FROM producto_terminado")
    suspend fun deleteAll()

    @Query("SELECT * FROM producto_terminado WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ProductoTerminadoEntity?
}