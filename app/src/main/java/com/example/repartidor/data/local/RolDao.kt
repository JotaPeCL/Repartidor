package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.RolEntity

@Dao
interface RolDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(roles: List<RolEntity>)

    @Query("SELECT * FROM roles")
    suspend fun getAll(): List<RolEntity>

    @Query("SELECT id FROM roles")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM roles WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)
}