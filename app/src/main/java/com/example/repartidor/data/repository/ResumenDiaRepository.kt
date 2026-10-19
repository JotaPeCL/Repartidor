
package com.example.repartidor.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.repartidor.data.local.AbonoDao
import com.example.repartidor.data.local.DevolucionDetalleDao
import com.example.repartidor.data.local.RotacionDao
import com.example.repartidor.data.local.RotacionDetalleDao
import com.example.repartidor.data.local.RotacionSustitucionDao
import com.example.repartidor.data.local.RutaDao
import com.example.repartidor.data.local.UsuarioDao
import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.local.VentaDetalleDao
import com.example.repartidor.data.model.dclass.ResumenDiaState
import com.example.repartidor.data.model.entity.RutaEntity
import com.example.repartidor.data.model.entity.UsuarioEntity
import java.time.LocalDate

class ResumenDiaRepository(
    private val ventaDao: VentaDao,
    private val abonoDao: AbonoDao,
    private val ventaDetalleDao: VentaDetalleDao,
    private val devolucionDetalleDao: DevolucionDetalleDao,
    private val usuarioDao: UsuarioDao,
    private val rutaDao: RutaDao,
    private val rotacionDao: RotacionDao,
    private val rotacionDetalleDao: RotacionDetalleDao,
    private val rotacionSustitucionDao: RotacionSustitucionDao
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerResumen(
        inicioDia: Long,
        finDia: Long,
        usuarioId: Int
    ): ResumenDiaState {

        val ventas = ventaDao.getTotalVentasDelDia(
            inicioDia,
            finDia,
            usuarioId
        )

        val dineroTotalVentas = ventaDao.getDineroTotalVentasDelDia(
            inicioDia,
            finDia,
            usuarioId
        )

        val abonos = abonoDao.getTotalAbonosDelDia(
            LocalDate.now().toString(),
            usuarioId
        )

        val cantidadCreditos =
            ventaDao.getCantidadCreditosPendientes(usuarioId)

        val totalPendiente =
            ventaDao.getTotalSaldoPendiente(usuarioId)

        val productosVendidos =
            ventaDetalleDao.getProductosVendidosDelDia(
                inicioDia,
                finDia,
                usuarioId
            )

        val productosDevueltos =
            devolucionDetalleDao.getProductosDevueltosDelDia(
                inicioDia,
                finDia,
                usuarioId
            )

        val totalDevoluciones =
            devolucionDetalleDao.getTotalDevolucionesDelDia(
                inicioDia,
                finDia,
                usuarioId
            )

        // =========================================
        // ROTACIONES DEL DÍA
        // =========================================

        val rotaciones = rotacionDao.getRotacionesDelDia(
            inicio = inicioDia,
            fin = finDia,
            usuarioId = usuarioId
        )

        val cantidadRotaciones = rotaciones.size

        // PRODUCTOS RECIBIDOS EN ROTACIONES
        val productosRecibidosRotacion =
            rotacionDetalleDao.getProductosRecibidosDelDia(
                inicioDia,
                finDia,
                usuarioId
            )

        // PRODUCTOS ENTREGADOS EN ROTACIONES
        val productosEntregadosRotacion =
            rotacionSustitucionDao.getProductosEntregadosDelDia(
                inicioDia,
                finDia,
                usuarioId
            )

        // =========================================
        // DINERO DE ROTACIONES
        // =========================================

        var totalRotacionesRecibido = 0.0
        var totalRotacionesEntregado = 0.0

        rotaciones.forEach { rotacion ->

            // Solo diferencias liquidadas en efectivo
            if (rotacion.formaLiquidacion == "EFECTIVO") {

                when (rotacion.tipoDiferencia) {

                    "CLIENTE_PAGO" -> {
                        totalRotacionesRecibido += rotacion.diferenciaDinero
                    }

                    "REPARTIDOR_PAGO" -> {
                        totalRotacionesEntregado += rotacion.diferenciaDinero
                    }
                }
            }
        }

        val balanceRotaciones =
            totalRotacionesRecibido - totalRotacionesEntregado

        val totalEfectivo =
            ventas + abonos + balanceRotaciones

        // =========================================
        // RESULTADO
        // =========================================

        return ResumenDiaState(
            efectivoVentas = ventas,
            dineroTotalVentas = dineroTotalVentas,
            totalAbonos = abonos,
            totalEfectivo = totalEfectivo,

            cantidadRotaciones = cantidadRotaciones,
            totalRotacionesRecibido = totalRotacionesRecibido,
            totalRotacionesEntregado = totalRotacionesEntregado,
            balanceRotaciones = balanceRotaciones,

            productosRecibidosRotacion = productosRecibidosRotacion,
            productosEntregadosRotacion = productosEntregadosRotacion,

            cantidadCreditos = cantidadCreditos,
            totalPendiente = totalPendiente,
            productosVendidos = productosVendidos,
            productosDevueltos = productosDevueltos,
            totalDevoluciones = totalDevoluciones
        )
    }

    suspend fun getUsuarioByUsername(username: String): UsuarioEntity? {
        return usuarioDao.getByUsername(username)
    }

    suspend fun getRutaByUsuarioId(usuarioId: Int): RutaEntity? {
        return rutaDao.getByUsuarioId(usuarioId)
    }
}
