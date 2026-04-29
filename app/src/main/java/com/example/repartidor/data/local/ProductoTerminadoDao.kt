package com.example.repartidor.data.local

import androidx.room.*
import com.example.repartidor.data.model.ProductoTerminadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoTerminadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lista: List<ProductoTerminadoEntity>)

    @Query("SELECT id FROM producto_terminado")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM producto_terminado WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM producto_terminado")
    suspend fun getAll(): List<ProductoTerminadoEntity>

    @Query("DELETE FROM producto_terminado")
    suspend fun deleteAll()

    @Query("SELECT * FROM producto_terminado WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ProductoTerminadoEntity?

    @Query("SELECT * FROM producto_terminado WHERE estado = 1")
    fun getProductos(): Flow<List<ProductoTerminadoEntity>>

    @Query("SELECT * FROM producto_terminado")
    fun getAllP(): Flow<List<ProductoTerminadoEntity>>
}