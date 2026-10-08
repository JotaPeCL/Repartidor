package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.entity.CancelacionDevolucionDetalleEntity
import com.example.repartidor.data.model.entity.CancelacionDevolucionEntity
import com.example.repartidor.data.model.entity.CancelacionDevolucionSustitucionEntity

@Dao
interface CancelacionDevolucionDao {

    @Insert
    suspend fun insertarCancelacion(
        cancelacion: CancelacionDevolucionEntity
    ): Long

    @Insert
    suspend fun insertarDetalles(
        detalles: List<CancelacionDevolucionDetalleEntity>
    )

    @Insert
    suspend fun insertarSustituciones(
        sustituciones: List<CancelacionDevolucionSustitucionEntity>
    )

    @Query(
        """
        SELECT * FROM cancelacion_devolucion
        ORDER BY fecha DESC
    """
    )
    suspend fun obtenerCancelaciones(): List<CancelacionDevolucionEntity>

    @Query(
        """
        SELECT * FROM cancelacion_devolucion
        WHERE sincronizado = 0
    """
    )
    suspend fun obtenerNoSincronizadas(): List<CancelacionDevolucionEntity>

    @Query(
        """
        SELECT * FROM cancelacion_devolucion_detalle
        WHERE cancelacionDevolucionId = :cancelacionId
    """
    )
    suspend fun obtenerDetalles(
        cancelacionId: Int
    ): List<CancelacionDevolucionDetalleEntity>

    @Query(
        """
        SELECT * FROM cancelacion_devolucion_sustitucion
        WHERE cancelacionDevolucionId = :cancelacionId
    """
    )
    suspend fun obtenerSustituciones(
        cancelacionId: Int
    ): List<CancelacionDevolucionSustitucionEntity>

    @Query(
        """
        SELECT * FROM cancelacion_devolucion_detalle
        WHERE cancelacionDevolucionUuid IN (:cancelacionUuids)
    """
    )
    suspend fun obtenerDetallesNoSincronizados(
        cancelacionUuids: List<String>
    ): List<CancelacionDevolucionDetalleEntity>

    @Query(
        """
        SELECT * FROM cancelacion_devolucion_sustitucion
        WHERE cancelacionDevolucionUuid IN (:cancelacionUuids)
    """
    )
    suspend fun obtenerSustitucionesNoSincronizadas(
        cancelacionUuids: List<String>
    ): List<CancelacionDevolucionSustitucionEntity>

    @Query(
        """
        UPDATE cancelacion_devolucion
        SET sincronizado = 1
        WHERE uuid = :uuid
    """
    )
    suspend fun marcarSincronizado(
        uuid: String
    )
}