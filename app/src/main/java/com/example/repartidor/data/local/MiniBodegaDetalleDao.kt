package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.ProductoConStock
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



}