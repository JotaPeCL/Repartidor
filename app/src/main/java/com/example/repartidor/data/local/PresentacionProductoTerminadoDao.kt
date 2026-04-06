package com.example.repartidor.data.local

import androidx.room.*
import com.example.repartidor.data.model.PresentacionProductoTerminadoEntity

@Dao
interface PresentacionProductoTerminadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lista: List<PresentacionProductoTerminadoEntity>)

    @Query("SELECT * FROM presentacion_producto")
    suspend fun getAll(): List<PresentacionProductoTerminadoEntity>

    @Query("DELETE FROM presentacion_producto")
    suspend fun deleteAll()
}