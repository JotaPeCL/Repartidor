package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.dclass.ProductoResumen
import com.example.repartidor.data.model.entity.RotacionDetalleEntity

@Dao
interface RotacionDetalleDao {

    @Insert
    suspend fun insertarDetalle(
        detalle: RotacionDetalleEntity
    ): Long

    @Insert
    suspend fun insertarDetalles(
        detalles: List<RotacionDetalleEntity>
    ): List<Long>

    @Query("""
        SELECT * FROM rotacion_detalle
        WHERE rotacionId = :rotacionId
    """)
    suspend fun obtenerPorRotacion(
        rotacionId: Int
    ): List<RotacionDetalleEntity>

    @Query("""
        SELECT * FROM rotacion_detalle
        WHERE rotacionUuid = :rotacionUuid
    """)
    suspend fun obtenerPorRotacionUuid(
        rotacionUuid: String
    ): List<RotacionDetalleEntity>

    // 🔹 PRODUCTOS RECIBIDOS EN ROTACIONES DEL DÍA
    @Query("""
    SELECT
        d.nombreProducto AS nombre,
        SUM(d.cantidad) AS cantidad
    FROM rotacion_detalle d
    INNER JOIN rotaciones r
        ON r.uuid = d.rotacionUuid
    WHERE CAST(r.fecha AS INTEGER) >= :inicioDia
      AND CAST(r.fecha AS INTEGER) < :finDia
      AND r.usuarioId = :usuarioId
    GROUP BY d.productoVariacionId, d.nombreProducto
    ORDER BY d.nombreProducto ASC
""")
    suspend fun getProductosRecibidosDelDia(
        inicioDia: Long,
        finDia: Long,
        usuarioId: Int
    ): List<ProductoResumen>
}