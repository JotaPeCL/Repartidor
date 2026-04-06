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

    @Query("SELECT * FROM cliente_dias_visita")
    suspend fun getAll(): List<ClienteDiasVisitaEntity>
}