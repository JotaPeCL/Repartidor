package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.ProductoConStock
import com.example.repartidor.data.model.ProductoVariacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoVariacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lista: List<ProductoVariacionEntity>)

    @Query("SELECT * FROM producto_variacion")
    suspend fun getAll(): List<ProductoVariacionEntity>

    @Query("DELETE FROM producto_variacion")
    suspend fun deleteAll()

    @Query("SELECT id FROM producto_variacion")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM producto_variacion WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)


    @Query("SELECT * FROM producto_variacion WHERE producto = :productoId")
    suspend fun getByProducto(productoId: Int): List<ProductoVariacionEntity>

    @Query("SELECT * FROM producto_variacion WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ProductoVariacionEntity?

    @Query("""
        SELECT 
            pv.id AS id,
            pv.producto AS productoId,
            pv.precio AS precio,
            IFNULL(mbd.cantidadActual, 0) AS stockActual,
            p.nombre AS presentacionNombre
        FROM producto_variacion pv
        INNER JOIN presentacion_producto p
            ON pv.presentacion = p.id
        LEFT JOIN mini_bodega_detalle mbd
            ON pv.id = mbd.productoVariacionId
            AND mbd.miniBodegaId = :miniBodegaId
        WHERE pv.producto = :productoId
    """)
    fun getVariacionesConStock(
        productoId: Int,
        miniBodegaId: Int
    ): Flow<List<ProductoConStock>>
}