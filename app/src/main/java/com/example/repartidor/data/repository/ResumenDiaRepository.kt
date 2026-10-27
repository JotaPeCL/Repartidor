
package com.example.repartidor.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.repartidor.data.local.AbonoDao
import com.example.repartidor.data.local.DevolucionDao
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
    private val rotacionSustitucionDao: RotacionSustitucionDao,
    private val devolucionDao: DevolucionDao
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerResumen(inicioDia: Long, finDia: Long, usuarioId: Int): ResumenDiaState {
        // VENTAS Y ABONOS
        val ventas = ventaDao.getTotalVentasDelDia(inicioDia, finDia, usuarioId)
        val dineroTotalVentas = ventaDao.getDineroTotalVentasDelDia(inicioDia, finDia, usuarioId)
        val abonos = abonoDao.getTotalAbonosDelDia(LocalDate.now().toString(), usuarioId)
        val cantidadCreditos = ventaDao.getCantidadCreditosPendientes(usuarioId)
        val totalPendiente = ventaDao.getTotalSaldoPendiente(usuarioId)

        // PRODUCTOS VENDIDOS Y DEVUELTOS
        val productosVendidos = ventaDetalleDao.getProductosVendidosDelDia(inicioDia, finDia, usuarioId)
        val productosDevueltos = devolucionDetalleDao.getProductosDevueltosDelDia(inicioDia, finDia, usuarioId)
        val totalDevoluciones = devolucionDetalleDao.getTotalDevolucionesDelDia(inicioDia, finDia, usuarioId)

        // ROTACIONES DEL DÍA
        val rotaciones = rotacionDao.getRotacionesDelDia(inicioDia, finDia, usuarioId)
        val cantidadRotaciones = rotaciones.size
        val productosRecibidosRotacion = rotacionDetalleDao.getProductosRecibidosDelDia(inicioDia, finDia, usuarioId)
        val productosEntregadosRotacion = rotacionSustitucionDao.getProductosEntregadosDelDia(inicioDia, finDia, usuarioId)

        // DINERO DE ROTACIONES
        var totalRotacionesRecibido = 0.0
        var totalRotacionesEntregado = 0.0

        rotaciones.forEach { rotacion ->
            if (rotacion.formaLiquidacion == "EFECTIVO") {
                when (rotacion.tipoDiferencia) {
                    "CLIENTE_PAGO" -> totalRotacionesRecibido += rotacion.diferenciaDinero
                    "REPARTIDOR_PAGO" -> totalRotacionesEntregado += rotacion.diferenciaDinero
                }
            }
        }

        val balanceRotaciones = totalRotacionesRecibido - totalRotacionesEntregado

        // DINERO DE DEVOLUCIONES
        val devoluciones = devolucionDao.getDevolucionesDelDia(inicioDia, finDia, usuarioId)
        var totalDevolucionesRecibido = 0.0
        var totalDevolucionesEntregado = 0.0

        devoluciones.forEach { devolucion ->
            if (devolucion.formaLiquidacion == "EFECTIVO") {
                when (devolucion.tipoDiferencia) {
                    "CLIENTE_PAGO" -> totalDevolucionesRecibido += devolucion.diferenciaDinero
                    "REPARTIDOR_PAGO" -> totalDevolucionesEntregado += devolucion.diferenciaDinero
                }
            }
        }

        val balanceDevoluciones = totalDevolucionesRecibido - totalDevolucionesEntregado

        // EFECTIVO TOTAL
        val totalEfectivo = ventas + abonos + balanceRotaciones + balanceDevoluciones

        // RESULTADO
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

            totalDevolucionesRecibido = totalDevolucionesRecibido,
            totalDevolucionesEntregado = totalDevolucionesEntregado,
            balanceDevoluciones = balanceDevoluciones,

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
