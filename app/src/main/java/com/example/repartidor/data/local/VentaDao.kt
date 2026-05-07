package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.model.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.PresentacionProductoTerminadoEntity
import com.example.repartidor.data.model.ProductoVariacionEntity
import com.example.repartidor.data.model.VentaDetalleEntity
import com.example.repartidor.data.model.VentaEntity

@Dao
interface VentaDao {

    @Insert
    suspend fun insert(venta: VentaEntity): Long

    @Insert
    suspend fun insertVenta(venta: VentaEntity): Long

    @Insert
    suspend fun insertDetalles(detalles: List<VentaDetalleEntity>)


    @Query("SELECT * FROM venta")
    suspend fun getAll(): List<VentaEntity>

    @Query(
        """
    SELECT * FROM mini_bodega_detalle 
    WHERE miniBodegaId = :miniBodegaId 
    AND productoVariacionId = :productoId
"""
    )
    suspend fun getProductoStock(
        miniBodegaId: Int,
        productoId: Int
    ): MiniBodegaDetalleEntity?

    @Update
    suspend fun updateMiniBodegaDetalle(detalle: MiniBodegaDetalleEntity)

    // 🔹 Ventas del día
    @Query("""
    SELECT * FROM venta 
    WHERE fecha BETWEEN :inicio AND :fin
    AND usuarioId = :usuarioId
    ORDER BY fecha DESC
""")
    suspend fun getVentasDelDia(
        inicio: String,
        fin: String,
        usuarioId: Int
    ): List<VentaEntity>


    // 🔹 Cliente por id
    @Query("SELECT * FROM cliente WHERE id = :clienteId")
    suspend fun getClienteById(clienteId: Int): ClienteEntity?


    // 🔹 Detalles de venta
    @Query("SELECT * FROM venta_detalle WHERE ventaId = :ventaId")
    suspend fun getDetallesByVentaId(ventaId: Int): List<VentaDetalleEntity>


    // 🔹 ProductoVariacion
    @Query("SELECT * FROM producto_variacion WHERE id = :id")
    suspend fun getProductoVariacionById(id: Int): ProductoVariacionEntity?


    // 🔹 Presentación
    @Query("SELECT * FROM presentacion_producto WHERE id = :id")
    suspend fun getPresentacionById(id: Int): PresentacionProductoTerminadoEntity?

}