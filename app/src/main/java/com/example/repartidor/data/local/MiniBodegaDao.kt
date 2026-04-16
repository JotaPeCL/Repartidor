package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.MiniBodegaEntity

@Dao
interface MiniBodegaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(miniBodegas: List<MiniBodegaEntity>)

    @Query("SELECT * FROM mini_bodega")
    suspend fun getAll(): List<MiniBodegaEntity>

    @Query("""
        SELECT * FROM mini_bodega 
        WHERE rutaId = :rutaId 
        ORDER BY fecha DESC 
        LIMIT 1
    """)
    suspend fun getUltimaPorRuta(rutaId: Int): MiniBodegaEntity?

    @Query("""
        SELECT * FROM mini_bodega 
        WHERE usuarioId = :userId
        LIMIT 1
    """)
    suspend fun getByUsuario(userId: Int): MiniBodegaEntity?

    @Query("SELECT * FROM mini_bodega WHERE estado = 1 LIMIT 1")
    suspend fun obtenerMiniBodegaActiva(): MiniBodegaEntity?
}