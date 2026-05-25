package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repartidor.data.model.AbonoEntity
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.model.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.PresentacionProductoTerminadoEntity
import com.example.repartidor.data.model.ProductoVariacionEntity
import com.example.repartidor.data.model.ProductoVenta
import com.example.repartidor.data.model.VentaCredito
import com.example.repartidor.data.model.VentaDetalleEntity
import com.example.repartidor.data.model.VentaEntity
import com.example.repartidor.data.model.VentaInfo

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
    @Query(
        """
    SELECT * FROM venta 
    WHERE fecha BETWEEN :inicio AND :fin
    AND usuarioId = :usuarioId
    ORDER BY fecha DESC
"""
    )
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

    @Insert
    suspend fun insertAbono(abono: AbonoEntity)

    @Query("SELECT SUM(monto) FROM abono WHERE ventaId = :ventaId")
    suspend fun getTotalAbonosByVentaId(ventaId: Int): Double?

    @Query(
        """
    SELECT 
    v.*,
    c.nombre,
    c.nombreNegocio,
    IFNULL(SUM(a.monto), 0) as totalAbonado,
    (v.total - IFNULL(SUM(a.monto), 0)) as saldoCalculado
FROM venta v
LEFT JOIN cliente c ON v.clienteId = c.id
LEFT JOIN abono a ON a.ventaId = v.id
WHERE v.tipoVenta = 'CREDITO'
AND (v.estadoPago = 'PENDIENTE' OR v.estadoPago = 'PARCIAL')
GROUP BY v.id"""
    )
    suspend fun getVentasCreditoPendientes(): List<VentaCredito>


    @Query(
        """
SELECT 
    v.id,
    v.fecha,
    v.tipoVenta,
    v.total,

    c.nombre AS clienteNombre,
    c.nombreNegocio AS clienteNegocio,
    c.porcentajeDescuento,

    IFNULL(SUM(a.monto), 0) AS totalAbonado,
    (v.total - IFNULL(SUM(a.monto), 0)) AS saldoPendiente

FROM venta v
INNER JOIN cliente c ON c.id = v.clienteId
LEFT JOIN abono a ON a.ventaId = v.id

WHERE v.id = :ventaId

GROUP BY v.id
"""
    )
    suspend fun getVentaInfo(ventaId: Int): VentaInfo

    @Query(
        """
SELECT 
    nombreProducto AS nombre,
    cantidad,
    precioUnitario
FROM venta_detalle
WHERE ventaId = :ventaId
"""
    )
    suspend fun getProductosVenta(ventaId: Int): List<ProductoVenta>

    @Query("SELECT * FROM venta WHERE id = :ventaId")
    suspend fun getVentaById(ventaId: Int): VentaEntity

    @Update
    suspend fun updateVenta(venta: VentaEntity)

    @Query("""
    SELECT IFNULL(SUM(total), 0.0)
    FROM venta
    WHERE fecha BETWEEN :inicioDia AND :finDia
    AND usuarioId = :usuarioId
    AND tipoVenta = 'CONTADO'
""")
    suspend fun getTotalVentasDelDia(
        inicioDia: Long,
        finDia: Long,
        usuarioId: Int
    ): Double

    @Query(
        """
    SELECT COUNT(*) 
    FROM venta
    WHERE saldoPendiente > 0
    AND usuarioId = :usuarioId
"""
    )
    suspend fun getCantidadCreditosPendientes(
        usuarioId: Int
    ): Int

    @Query(
        """
    SELECT IFNULL(SUM(saldoPendiente), 0.0)
    FROM venta
    WHERE saldoPendiente > 0
    AND usuarioId = :usuarioId
"""
    )
    suspend fun getTotalSaldoPendiente(
        usuarioId: Int
    ): Double

    @Query("""
    SELECT * FROM venta 
    WHERE sincronizado = 0 
    AND fecha BETWEEN :inicio AND :fin
""")
    suspend fun getVentasNoSincronizadas(inicio: Long, fin: Long): List<VentaEntity>

    @Query("SELECT * FROM venta WHERE sincronizado = 0")
    suspend fun obtenerVentasNoSincronizadas(): List<VentaEntity>

    @Query("""
        UPDATE venta 
        SET sincronizado = 1 
        WHERE uuid = :uuid
    """)
    suspend fun marcarSincronizado(uuid: String)

}