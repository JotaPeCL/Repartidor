package com.example.repartidor.data.local

import androidx.room.*
import com.example.repartidor.data.model.CategoriaProductoEntity

@Dao
interface CategoriaProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lista: List<CategoriaProductoEntity>)

    @Query("SELECT id FROM categoria_producto")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM categoria_producto WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)


    @Query("SELECT * FROM categoria_producto")
    suspend fun getAll(): List<CategoriaProductoEntity>

    @Query("DELETE FROM categoria_producto")
    suspend fun deleteAll()
}