package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.DevolucionDetalleEntity
import com.example.repartidor.data.model.ProductoResumen

@Dao
interface DevolucionDetalleDao {

    @Insert
    suspend fun insertAll(detalles: List<DevolucionDetalleEntity>)

    @Query("SELECT * FROM devolucion_detalle WHERE devolucionId = :id")
    suspend fun getByDevolucion(id: Int): List<DevolucionDetalleEntity>

    @Query("""
    SELECT 
        (pt.nombre || ' ' || pp.nombre) as nombre,
        SUM(dd.cantidad) as cantidad
    FROM devolucion_detalle dd
    INNER JOIN devoluciones d ON d.id = dd.devolucionId
    INNER JOIN producto_variacion pv ON pv.id = dd.productoVariacionId
    INNER JOIN producto_terminado pt ON pt.id = pv.producto
    INNER JOIN presentacion_producto pp ON pp.id = pv.presentacion
    WHERE d.fecha BETWEEN :inicioDia AND :finDia
    AND d.usuarioId = :usuarioId
    GROUP BY dd.productoVariacionId
""")
    suspend fun getProductosDevueltosDelDia(
        inicioDia: Long,
        finDia: Long,
        usuarioId: Int
    ): List<ProductoResumen>
}