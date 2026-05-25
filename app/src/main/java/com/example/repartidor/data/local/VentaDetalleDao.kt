package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repartidor.data.model.ProductoResumen
import com.example.repartidor.data.model.VentaDetalleEntity

@Dao
interface VentaDetalleDao {

    @Insert
    suspend fun insertAll(detalles: List<VentaDetalleEntity>)

    @Query("SELECT * FROM venta_detalle WHERE ventaId = :ventaId")
    suspend fun getByVenta(ventaId: Int): List<VentaDetalleEntity>

    @Query("""
    SELECT 
        (pt.nombre || ' ' || pp.nombre) as nombre,
        SUM(vd.cantidad) as cantidad
    FROM venta_detalle vd
    INNER JOIN venta v ON v.id = vd.ventaId
    INNER JOIN producto_variacion pv ON pv.id = vd.productoVariacionId
    INNER JOIN producto_terminado pt ON pt.id = pv.producto
    INNER JOIN presentacion_producto pp ON pp.id = pv.presentacion
    WHERE v.fecha BETWEEN :inicioDia AND :finDia
    AND v.usuarioId = :usuarioId
    GROUP BY vd.productoVariacionId
""")
    suspend fun getProductosVendidosDelDia(
        inicioDia: Long,
        finDia: Long,
        usuarioId: Int
    ): List<ProductoResumen>

    @Query("""
    SELECT * FROM venta_detalle 
    WHERE ventaUuid IN (:uuids)
""")
    suspend fun getDetallesByVentaUuids(uuids: List<String>): List<VentaDetalleEntity>


}