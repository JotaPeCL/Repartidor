package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.ProductoVariacionEntity

@Dao
interface ProductoVariacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lista: List<ProductoVariacionEntity>)

    @Query("SELECT * FROM producto_variacion")
    suspend fun getAll(): List<ProductoVariacionEntity>

    @Query("DELETE FROM producto_variacion")
    suspend fun deleteAll()


    @Query("SELECT * FROM producto_variacion WHERE producto = :productoId")
    suspend fun getByProducto(productoId: Int): List<ProductoVariacionEntity>

    @Query("SELECT * FROM producto_variacion WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ProductoVariacionEntity?
}