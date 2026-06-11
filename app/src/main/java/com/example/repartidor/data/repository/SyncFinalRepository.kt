package com.example.repartidor.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.repartidor.data.local.AbonoDao
import com.example.repartidor.data.local.DevolucionDao
import com.example.repartidor.data.local.DevolucionDetalleDao
import com.example.repartidor.data.local.MiniBodegaDetalleMermaDao
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.local.VentaDetalleDao
import com.example.repartidor.data.remote.request.AbonoRequest
import com.example.repartidor.data.remote.request.DevolucionDetalleRequest
import com.example.repartidor.data.remote.request.DevolucionRequest
import com.example.repartidor.data.remote.request.MermaRequest
import com.example.repartidor.data.remote.RetrofitClient
import com.example.repartidor.data.remote.request.SyncAbonosRequest
import com.example.repartidor.data.remote.request.SyncDevolucionesRequest
import com.example.repartidor.data.remote.request.SyncVentasRequest
import com.example.repartidor.data.remote.request.VentaDetalleRequest
import com.example.repartidor.data.remote.request.VentaRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SyncFinalRepository(
    private val ventaDao: VentaDao,
    private val ventaDetalleDao: VentaDetalleDao,
    private val abonoDao: AbonoDao,
    private val devolucionDao: DevolucionDao,
    private val devolucionDetalleDao: DevolucionDetalleDao,
    private val mermaDao: MiniBodegaDetalleMermaDao,
    private val sessionManager: SessionManager
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncTodo(inicio: Long, fin: Long, fechaHoy: String): Result<Unit> {
        return try {

            val usuarioId = sessionManager.getUserId()
                ?: return Result.failure(Exception("No user"))
            val miniBodegaId = sessionManager.getMiniBodegaId()
                ?: return Result.failure(Exception("No minibodega"))

            // =========================
            // 🔥 VENTAS
            // =========================
            val ventas = ventaDao.getVentasNoSincronizadas(inicio, fin)

            val detallesVenta = ventaDetalleDao.getDetallesByVentaUuids(
                ventas.map { it.uuid }
            )

            if (ventas.isNotEmpty()) {

                val ventasRequest = ventas.map {
                    VentaRequest(
                        uuid = it.uuid,
                        usuario_id = usuarioId,
                        cliente_id = it.clienteId,
                        total = it.total,
                        tipo_venta = it.tipoVenta,
                        fecha = normalizarFecha(it.fecha)
                    )
                }

                val detallesRequest = detallesVenta.map {
                    VentaDetalleRequest(
                        uuid = it.uuid,
                        venta_uuid = it.ventaUuid,
                        producto_variacion_id = it.productoVariacionId,
                        cantidad = it.cantidad,
                        precio_unitario = it.precioUnitario,
                        nombre_producto = it.nombreProducto
                    )
                }

                val response = RetrofitClient.api.syncVentas(
                    SyncVentasRequest(ventasRequest, detallesRequest)
                )

                if (!response.isSuccessful) {
                    return Result.failure(Exception("Error ventas ${response.code()}"))
                }

                ventas.forEach {
                    ventaDao.marcarSincronizado(it.uuid)
                }
            }

            // =========================
            // 🔥 ABONOS (usa TU query por fecha)
            // =========================
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            val inicioStr = Instant.ofEpochMilli(inicio)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter)

            val finStr = Instant.ofEpochMilli(fin)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter)
            val abonos = abonoDao.getAbonosNoSincronizados(inicioStr, finStr)

            if (abonos.isNotEmpty()) {
                //fecha = normalizarFecha(it.fecha)
                val abonosRequest = abonos.map {
                    AbonoRequest(
                        uuid = it.uuid,
                        venta_uuid = it.ventaUuid,
                        usuario_id = usuarioId,
                        monto = it.monto,
                        fecha = normalizarFecha(it.fecha)
                    )
                }

                val response = RetrofitClient.api.syncAbonos(
                    SyncAbonosRequest(abonosRequest)
                )

                if (!response.isSuccessful) {
                    return Result.failure(Exception("Error abonos ${response.code()}"))
                }

                abonos.forEach {
                    abonoDao.marcarSincronizado(it.uuid)
                }
            }

            // =========================
            // 🔥 DEVOLUCIONES + DETALLES + MERMAS
            // =========================
            val devoluciones = devolucionDao.getDevolucionesNoSincronizadas(inicio, fin)

            if (devoluciones.isNotEmpty()) {

                val detalles = devolucionDetalleDao.getDetallesByDevolucionUuids(
                    devoluciones.map { it.uuid }
                )

                val mermas = mermaDao.getMermasByDevolucionUuids(
                    devoluciones.map { it.uuid }
                )

                val devolucionesRequest = devoluciones.map {
                    DevolucionRequest(
                        uuid = it.uuid,
                        tipo = it.tipo,
                        cliente_id = it.clienteId,
                        usuario_id = usuarioId,
                        mini_bodega_id = miniBodegaId,
                        fecha = normalizarFecha(it.fecha),
                        descripcion = it.descripcion
                    )
                }

                val detallesRequest = detalles.map {
                    DevolucionDetalleRequest(
                        uuid = it.uuid,
                        devolucion_uuid = it.devolucionUuid,
                        producto_variacion_id = it.productoVariacionId,
                        cantidad = it.cantidad,
                        precio_unitario = it.precioUnitario
                    )
                }

                val mermasRequest = mermas.map {
                    MermaRequest(
                        uuid = it.uuid,
                        mini_bodega_id = it.miniBodegaId,
                        producto_variacion_id = it.productoVariacionId,
                        cantidad = it.cantidad,
                        devolucion_uuid = it.devolucionUuid
                    )
                }

                val response = RetrofitClient.api.syncDevoluciones(
                    SyncDevolucionesRequest(
                        devolucionesRequest,
                        detallesRequest,
                        mermasRequest
                    )
                )

                if (!response.isSuccessful) {
                    return Result.failure(Exception("Error devoluciones ${response.code()}"))
                }

                devoluciones.forEach {
                    devolucionDao.marcarSincronizado(it.uuid)
                }
                mermas.forEach {
                    mermaDao.marcarSincronizado(it.uuid)
                }
            }

            // =========================
            // ✅ TODO OK
            // =========================
            sessionManager.setFinalDia(true)
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun normalizarFecha(fecha: Any): String {
        val zone = java.time.ZoneId.systemDefault()
        val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

        return when (fecha) {

            is Long -> {
                // viene como millis
                java.time.Instant.ofEpochMilli(fecha)
                    .atZone(zone)
                    .toLocalDateTime()
                    .format(formatter)
            }

            is String -> {
                try {
                    val parsed = LocalDateTime.parse(
                        fecha,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
                    parsed.format(formatter)
                } catch (e: Exception) {
                    LocalDateTime.now().format(formatter) // fallback real
                }
            }

            else -> {
                throw Exception("Formato de fecha no soportado")
            }
        }
    }

}