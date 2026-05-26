package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.MiniBodegaDetalleMermaEntity

@Dao
interface MiniBodegaDetalleMermaDao {

    @Insert
    suspend fun insert(merma: MiniBodegaDetalleMermaEntity)

    @Query("SELECT * FROM mini_bodega_detalle_merma")
    suspend fun getAll(): List<MiniBodegaDetalleMermaEntity>

    @Insert
    suspend fun insertarMerma(merma: MiniBodegaDetalleMermaEntity)

    @Query("""
    SELECT * FROM mini_bodega_detalle_merma 
    WHERE devolucionUuid IN (:uuids)
""")
    suspend fun getMermasByDevolucionUuids(uuids: List<String>): List<MiniBodegaDetalleMermaEntity>

    @Query("""
    UPDATE mini_bodega_detalle_merma
    SET sincronizado = 1
    WHERE uuid = :uuid
""")
    suspend fun marcarSincronizado(uuid: String)

    @Query("""
    DELETE FROM mini_bodega_detalle_merma
    WHERE sincronizado = 1
""")
    suspend fun deleteSincronizados()
}