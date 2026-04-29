package com.example.repartidor.data.local

import androidx.room.*
import com.example.repartidor.data.model.PresentacionProductoTerminadoEntity

@Dao
interface PresentacionProductoTerminadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lista: List<PresentacionProductoTerminadoEntity>)

    @Query("SELECT id FROM presentacion_producto")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM presentacion_producto WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM presentacion_producto")
    suspend fun getAll(): List<PresentacionProductoTerminadoEntity>

    @Query("DELETE FROM presentacion_producto")
    suspend fun deleteAll()

    @Query("SELECT * FROM presentacion_producto WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PresentacionProductoTerminadoEntity?
}