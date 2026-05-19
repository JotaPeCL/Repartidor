package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.AbonoEntity

@Dao
interface AbonoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(abono: AbonoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(abonos: List<AbonoEntity>)

    @Query("SELECT * FROM abono WHERE ventaId = :ventaId")
    suspend fun obtenerPorVenta(ventaId: Int): List<AbonoEntity>

    @Query("SELECT * FROM abono WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizados(): List<AbonoEntity>

    @Query("UPDATE abono SET sincronizado = 1 WHERE uuid IN (:uuids)")
    suspend fun marcarComoSincronizados(uuids: List<String>)

    @Delete
    suspend fun eliminar(abono: AbonoEntity)

    @Insert
    suspend fun insertAbono(abono: AbonoEntity)

}