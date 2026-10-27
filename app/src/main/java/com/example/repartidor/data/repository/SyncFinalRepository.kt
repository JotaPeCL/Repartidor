package com.example.repartidor.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.repartidor.data.local.AbonoDao
import com.example.repartidor.data.local.CancelacionDevolucionDao
import com.example.repartidor.data.local.CancelacionRotacionDao
import com.example.repartidor.data.local.CancelacionRotacionDetalleDao
import com.example.repartidor.data.local.CancelacionVentaDao
import com.example.repartidor.data.local.DevolucionDao
import com.example.repartidor.data.local.DevolucionDetalleDao
import com.example.repartidor.data.local.MiniBodegaDetalleMermaDao
import com.example.repartidor.data.local.DevolucionSustitucionDao
import com.example.repartidor.data.local.RotacionDao
import com.example.repartidor.data.local.RotacionDetalleDao
import com.example.repartidor.data.local.RotacionSustitucionDao
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.local.VentaDetalleDao
import com.example.repartidor.data.remote.request.AbonoRequest
import com.example.repartidor.data.remote.request.DevolucionDetalleRequest
import com.example.repartidor.data.remote.request.DevolucionRequest
import com.example.repartidor.data.remote.request.MermaRequest
import com.example.repartidor.data.remote.RetrofitClient
import com.example.repartidor.data.remote.request.CancelacionDevolucionDetalleRequest
import com.example.repartidor.data.remote.request.CancelacionDevolucionRequest
import com.example.repartidor.data.remote.request.CancelacionDevolucionSustitucionRequest
import com.example.repartidor.data.remote.request.CancelacionRotacionDetalleRequest
import com.example.repartidor.data.remote.request.CancelacionRotacionRequest
import com.example.repartidor.data.remote.request.CancelacionVentaDetalleRequest
import com.example.repartidor.data.remote.request.CancelacionVentaRequest
import com.example.repartidor.data.remote.request.RotacionDetalleRequest
import com.example.repartidor.data.remote.request.RotacionRequest
import com.example.repartidor.data.remote.request.RotacionSustitucionRequest
import com.example.repartidor.data.remote.request.SustitucionRequest
import com.example.repartidor.data.remote.request.SyncAbonosRequest
import com.example.repartidor.data.remote.request.SyncCancelacionesDevolucionesRequest
import com.example.repartidor.data.remote.request.SyncCancelacionesRequest
import com.example.repartidor.data.remote.request.SyncCancelacionesRotacionesRequest
import com.example.repartidor.data.remote.request.SyncDevolucionesRequest
import com.example.repartidor.data.remote.request.SyncRotacionesRequest
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
    private val cancelacionVentaDao: CancelacionVentaDao,
    private val devolucionSustitucionDao: DevolucionSustitucionDao,
    private val cancelacionDevolucionDao: CancelacionDevolucionDao,
    private val rotacionDao: RotacionDao,
    private val rotacionDetalleDao: RotacionDetalleDao,
    private val rotacionSustitucionDao: RotacionSustitucionDao,
    private val cancelacionRotacionDao: CancelacionRotacionDao,
    private val cancelacionRotacionDetalleDao: CancelacionRotacionDetalleDao,
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

                val sustituciones = devolucionSustitucionDao.getSustitucionesByDevolucionUuids(
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
                        descripcion = it.descripcion,
                        diferencia_dinero = it.diferenciaDinero,
                        tipo_diferencia = it.tipoDiferencia,
                        forma_liquidacion = it.formaLiquidacion
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

                val detallesPorId = detalles.associateBy { it.id }

                val sustitucionesRequest = sustituciones.map { s ->
                    val detalle = detallesPorId[s.devolucionDetalleId]
                        ?: throw Exception(
                            "Detalle de devolución no encontrado para sustitución ${s.uuid}"
                        )

                    SustitucionRequest(
                        uuid = s.uuid,
                        devolucion_uuid = s.devolucionUuid,
                        devolucion_detalle_uuid = detalle.uuid,
                        producto_variacion_id = s.productoVariacionId,
                        cantidad = s.cantidad,
                        precio_unitario = s.precioUnitario
                    )
                }

                val response = RetrofitClient.api.syncDevoluciones(
                    SyncDevolucionesRequest(
                        devolucionesRequest,
                        detallesRequest,
                        mermasRequest,
                        sustitucionesRequest
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
                sustituciones.forEach {
                    devolucionSustitucionDao.marcarSincronizado(it.uuid)
                }
            }

            // =========================
            // 🔥 CANCELACIONES
            // =========================
            val cancelaciones = cancelacionVentaDao.obtenerNoSincronizadas()

            if (cancelaciones.isNotEmpty()) {

                val detalles = cancelacionVentaDao.obtenerDetallesNoSincronizados(
                    cancelaciones.map { it.uuid }
                )

                val cancelacionesRequest = cancelaciones.map {
                    CancelacionVentaRequest(
                        uuid = it.uuid,
                        venta_uuid = it.ventaUuid,
                        usuario_id = usuarioId,
                        mini_bodega_id = miniBodegaId,
                        fecha = normalizarFecha(it.fecha),
                        total = it.total,
                        motivo = it.motivo
                    )
                }

                val detallesRequest = detalles.map {
                    CancelacionVentaDetalleRequest(
                        uuid = it.uuid,
                        cancelacion_uuid = it.cancelacionVentaUuid,
                        producto_variacion_id = it.productoVariacionId,
                        nombre_producto = it.nombreProducto,
                        cantidad = it.cantidad,
                        precio_unitario = it.precioUnitario
                    )
                }

                val response = RetrofitClient.api.syncCancelaciones(
                    SyncCancelacionesRequest(
                        cancelaciones = cancelacionesRequest,
                        detalles = detallesRequest
                    )
                )

                if (!response.isSuccessful) {
                    return Result.failure(
                        Exception("Error cancelaciones ${response.code()}")
                    )
                }

                cancelaciones.forEach {
                    cancelacionVentaDao.marcarSincronizado(it.uuid)
                }
            }

            // =========================
            // 🔥 CANCELACIONES DE DEVOLUCIONES
            // =========================
            val cancelacionesDevoluciones =
                cancelacionDevolucionDao.obtenerNoSincronizadas()

            if (cancelacionesDevoluciones.isNotEmpty()) {

                val detalles = cancelacionDevolucionDao.obtenerDetallesNoSincronizados(
                    cancelacionesDevoluciones.map { it.uuid }
                )

                val sustituciones =
                    cancelacionDevolucionDao.obtenerSustitucionesNoSincronizadas(
                        cancelacionesDevoluciones.map { it.uuid }
                    )

                val cancelacionesRequest = cancelacionesDevoluciones.map {
                    CancelacionDevolucionRequest(
                        uuid = it.uuid,
                        devolucion_uuid = it.devolucionUuid,
                        usuario_id = usuarioId,
                        mini_bodega_id = miniBodegaId,
                        fecha = normalizarFecha(it.fecha),
                        total = it.total,
                        motivo = it.motivo,
                        diferencia_dinero = it.diferenciaDinero,
                        tipo_diferencia = it.tipoDiferencia,
                        forma_liquidacion = it.formaLiquidacion
                    )
                }

                val detallesRequest = detalles.map {
                    CancelacionDevolucionDetalleRequest(
                        uuid = it.uuid,
                        cancelacion_uuid = it.cancelacionDevolucionUuid,
                        producto_variacion_id = it.productoVariacionId,
                        nombre_producto = it.nombreProducto,
                        cantidad = it.cantidad,
                        precio_unitario = it.precioUnitario
                    )
                }

                val sustitucionesRequest = sustituciones.map {
                    CancelacionDevolucionSustitucionRequest(
                        uuid = it.uuid,
                        cancelacion_uuid = it.cancelacionDevolucionUuid,
                        producto_variacion_id = it.productoVariacionId,
                        nombre_producto = it.nombreProducto,
                        cantidad = it.cantidad,
                        precio_unitario = it.precioUnitario
                    )
                }

                val response = RetrofitClient.api.syncCancelacionesDevoluciones(
                    SyncCancelacionesDevolucionesRequest(
                        cancelaciones = cancelacionesRequest,
                        detalles = detallesRequest,
                        sustituciones = sustitucionesRequest
                    )
                )

                if (!response.isSuccessful) {
                    return Result.failure(
                        Exception(
                            "Error cancelaciones devoluciones ${response.code()}"
                        )
                    )
                }

                cancelacionesDevoluciones.forEach {
                    cancelacionDevolucionDao.marcarSincronizado(it.uuid)
                }
            }


            // =========================
            // 🔥 ROTACIONES + DETALLES + SUSTITUCIONES
            // =========================
            val rotaciones = rotacionDao.getPendientes()

            if (rotaciones.isNotEmpty()) {

                val detalles = rotaciones.flatMap {
                    rotacionDetalleDao.obtenerPorRotacionUuid(it.uuid)
                }

                val sustituciones = rotacionSustitucionDao.getPendientesPorRotaciones(
                    rotaciones.map { it.uuid }
                )

                val rotacionesRequest = rotaciones.map {
                    RotacionRequest(
                        uuid = it.uuid,
                        cliente_id = it.clienteId,
                        usuario_id = it.usuarioId,
                        mini_bodega_id = it.miniBodegaId,
                        fecha = fechaRotacionISO(it.fecha),
                        descripcion = it.descripcion,

                        // DIFERENCIA ECONÓMICA
                        diferencia_dinero = it.diferenciaDinero,
                        tipo_diferencia = it.tipoDiferencia,
                        forma_liquidacion = it.formaLiquidacion,

                        created_at = fechaRotacionISO(it.createdAt)
                    )
                }

                val detallesRequest = detalles.map {
                    RotacionDetalleRequest(
                        uuid = it.uuid,
                        rotacion_uuid = it.rotacionUuid,
                        producto_variacion_id = it.productoVariacionId,
                        nombre_producto = it.nombreProducto,
                        cantidad = it.cantidad,
                        precio_unitario = it.precioUnitario
                    )
                }

                val detallesPorId = detalles.associateBy { it.id }

                val sustitucionesRequest = sustituciones.map { s ->
                    val detalle = detallesPorId[s.rotacionDetalleId]
                        ?: throw Exception(
                            "Detalle de rotación no encontrado para sustitución ${s.uuid}"
                        )

                    RotacionSustitucionRequest(
                        uuid = s.uuid,
                        rotacion_uuid = s.rotacionUuid,
                        rotacion_detalle_uuid = detalle.uuid,
                        producto_variacion_id = s.productoVariacionId,
                        nombre_producto = s.nombreProducto,
                        cantidad = s.cantidad,
                        precio_unitario = s.precioUnitario,
                        created_at = fechaRotacionISO(s.createdAt)
                    )
                }

                val response = RetrofitClient.api.syncRotaciones(
                    SyncRotacionesRequest(
                        rotaciones = rotacionesRequest,
                        detalles = detallesRequest,
                        sustituciones = sustitucionesRequest
                    )
                )

                if (!response.isSuccessful) {
                    return Result.failure(
                        Exception("Error rotaciones ${response.code()}")
                    )
                }

                rotaciones.forEach {
                    rotacionDao.marcarSincronizado(it.uuid)
                }

                sustituciones.forEach {
                    rotacionSustitucionDao.marcarSincronizado(it.uuid)
                }
            }

            // =========================
// CANCELACIONES DE ROTACIONES
// =========================
            val cancelacionesRotaciones = cancelacionRotacionDao.getPendientes()

            if (cancelacionesRotaciones.isNotEmpty()) {
                val detalles = cancelacionesRotaciones.flatMap {
                    cancelacionRotacionDetalleDao.obtenerPorCancelacionUuid(it.uuid)
                }

                val cancelacionesRequest = cancelacionesRotaciones.map {
                    CancelacionRotacionRequest(
                        uuid = it.uuid,
                        rotacion_uuid = it.rotacionUuid,
                        usuario_id = it.usuarioId,
                        mini_bodega_id = it.miniBodegaId,
                        fecha = it.fecha,
                        descripcion = it.descripcion,
                        diferencia_dinero = it.diferenciaDinero,
                        tipo_diferencia = it.tipoDiferencia,
                        forma_liquidacion = it.formaLiquidacion,
                        motivo = it.motivo,
                        created_at = it.createdAt
                    )
                }

                val detallesRequest = detalles.map {
                    CancelacionRotacionDetalleRequest(
                        uuid = it.uuid,
                        cancelacion_uuid = it.cancelacionRotacionUuid,
                        producto_variacion_id = it.productoVariacionId,
                        nombre_producto = it.nombreProducto,
                        cantidad = it.cantidad,
                        precio_unitario = it.precioUnitario,
                        tipo_movimiento = it.tipoMovimiento
                    )
                }

                val response = RetrofitClient.api.syncCancelacionesRotaciones(
                    SyncCancelacionesRotacionesRequest(
                        cancelaciones = cancelacionesRequest,
                        detalles = detallesRequest
                    )
                )

                if (!response.isSuccessful) {
                    return Result.failure(Exception("Error cancelaciones rotaciones ${response.code()}: ${response.errorBody()?.string()}"))
                }

                cancelacionesRotaciones.forEach {
                    cancelacionRotacionDao.marcarSincronizado(it.uuid)
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
        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        return when (fecha) {
            is Long -> {
                Instant.ofEpochMilli(fecha)
                    .atZone(zone)
                    .toLocalDateTime()
                    .format(formatter)
            }

            is String -> {
                val millis = fecha.toLongOrNull()

                when {
                    millis != null -> {
                        Instant.ofEpochMilli(millis)
                            .atZone(zone)
                            .toLocalDateTime()
                            .format(formatter)
                    }

                    else -> {
                        try {
                            LocalDateTime.parse(
                                fecha.replace(" ", "T"),
                                formatter
                            ).format(formatter)
                        } catch (e: Exception) {
                            throw IllegalArgumentException("Fecha inválida: $fecha", e)
                        }
                    }
                }
            }

            else -> throw IllegalArgumentException("Formato de fecha no soportado: $fecha")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fechaRotacionISO(fecha: String): String {
        val millis = fecha.toLongOrNull()
            ?: throw Exception("Fecha de rotación inválida: $fecha")

        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

}