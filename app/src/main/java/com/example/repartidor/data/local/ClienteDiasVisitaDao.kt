package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.ClienteDiasVisitaEntity

@Dao
interface ClienteDiasVisitaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dias: List<ClienteDiasVisitaEntity>)

    @Query("SELECT id FROM cliente_dias_visita")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM cliente_dias_visita WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM cliente_dias_visita")
    suspend fun getAll(): List<ClienteDiasVisitaEntity>
}