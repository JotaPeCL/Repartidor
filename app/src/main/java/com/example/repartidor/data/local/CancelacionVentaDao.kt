package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.entity.CancelacionVentaEntity
import com.example.repartidor.data.model.entity.CancelacionVentaDetalleEntity

@Dao
interface CancelacionVentaDao {

    @Insert
    suspend fun insertarCancelacion(cancelacion: CancelacionVentaEntity): Long

    @Insert
    suspend fun insertarDetalles(detalles: List<CancelacionVentaDetalleEntity>)

    @Query("SELECT * FROM cancelacion_venta ORDER BY fecha DESC")
    suspend fun obtenerCancelaciones(): List<CancelacionVentaEntity>

    @Query("SELECT * FROM cancelacion_venta WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizadas(): List<CancelacionVentaEntity>

    @Query("SELECT * FROM cancelacion_venta_detalle WHERE cancelacionVentaId = :cancelacionId")
    suspend fun obtenerDetalles(cancelacionId: Int): List<CancelacionVentaDetalleEntity>

    @Query("""SELECT * FROM cancelacion_venta_detalle WHERE cancelacionVentaUuid IN (:cancelacionUuids)""")
    suspend fun obtenerDetallesNoSincronizados(
        cancelacionUuids: List<String>
    ): List<CancelacionVentaDetalleEntity>

    @Query("""
    UPDATE cancelacion_venta
    SET sincronizado = 1
    WHERE uuid = :uuid
""")
    suspend fun marcarSincronizado(uuid: String)

}