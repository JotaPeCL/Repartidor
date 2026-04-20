package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.ProductoConStock
import com.example.repartidor.data.model.ProductoTerminadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MiniBodegaDetalleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(detalles: List<MiniBodegaDetalleEntity>)


    @Query("""
        SELECT * FROM mini_bodega_detalle 
        WHERE miniBodegaId = :miniBodegaId
    """)
    suspend fun getByMiniBodega(miniBodegaId: Int): List<MiniBodegaDetalleEntity>

    @Query("""
    SELECT 
        pv.id, 
        pv.producto as productoId, 
        pv.precio,
        mbd.cantidadActual as stockActual,
        pp.nombre as presentacionNombre
    FROM mini_bodega_detalle mbd
    INNER JOIN producto_variacion pv 
        ON pv.id = mbd.productoVariacionId
    INNER JOIN presentacion_producto pp
        ON pv.presentacion = pp.id
    WHERE pv.producto = :productoId
    AND mbd.miniBodegaId = :miniBodegaId
    AND mbd.cantidadActual > 0
""")
    fun getVariacionesConStock(
        productoId: Int,
        miniBodegaId: Int
    ): Flow<List<ProductoConStock>>

    @Query("""
    SELECT 
        pv.id, 
        pv.producto as productoId, 
        pv.precio,
        IFNULL(mbd.cantidadActual, 0) as stockActual,
        pp.nombre as presentacionNombre
    FROM producto_variacion pv
    INNER JOIN presentacion_producto pp
        ON pv.presentacion = pp.id
    LEFT JOIN mini_bodega_detalle mbd 
        ON pv.id = mbd.productoVariacionId
        AND mbd.miniBodegaId = :miniBodegaId
    WHERE pv.producto = :productoId
""")
    fun getVariacionesParaReabastecimiento(
        productoId: Int,
        miniBodegaId: Int
    ): Flow<List<ProductoConStock>>

    @Query("SELECT * FROM mini_bodega_detalle WHERE miniBodegaId = :miniBodegaId")
    suspend fun obtenerDetallesPorMiniBodega(miniBodegaId: Int): List<MiniBodegaDetalleEntity>

    @Query("""
    SELECT DISTINCT pt.*
    FROM producto_terminado pt
    INNER JOIN producto_variacion pv 
        ON pv.producto = pt.id
    INNER JOIN mini_bodega_detalle mbd 
        ON mbd.productoVariacionId = pv.id
    WHERE mbd.miniBodegaId = :miniBodegaId
    AND mbd.cantidadActual > 0
    AND pt.estado = 1
""")
    fun getProductosConStock(miniBodegaId: Int): Flow<List<ProductoTerminadoEntity>>
}