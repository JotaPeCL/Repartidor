package com.example.repartidor.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.repartidor.data.local.AbonoDao
import com.example.repartidor.data.local.DevolucionDetalleDao
import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.local.VentaDetalleDao
import com.example.repartidor.data.model.ResumenDiaState
import java.time.LocalDate

class ResumenDiaRepository(
    private val ventaDao: VentaDao,
    private val abonoDao: AbonoDao,
    private val ventaDetalleDao: VentaDetalleDao,
    private val devolucionDetalleDao: DevolucionDetalleDao
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerResumen(
        inicioDia: Long,
        finDia: Long,
        usuarioId: Int
    ): ResumenDiaState {

        val ventas = ventaDao.getTotalVentasDelDia(inicioDia, finDia, usuarioId)
        val abonos = abonoDao.getTotalAbonosDelDia( LocalDate.now().toString(), usuarioId)

        val cantidadCreditos = ventaDao.getCantidadCreditosPendientes(usuarioId)
        val totalPendiente = ventaDao.getTotalSaldoPendiente(usuarioId)

        val productosVendidos =
            ventaDetalleDao.getProductosVendidosDelDia(inicioDia, finDia, usuarioId)

        val productosDevueltos =
            devolucionDetalleDao.getProductosDevueltosDelDia(inicioDia, finDia, usuarioId)

        return ResumenDiaState(
            efectivoVentas = ventas,
            totalAbonos = abonos,
            totalEfectivo = ventas + abonos, // 🔥 cálculo aquí (NO en UI)
            cantidadCreditos = cantidadCreditos,
            totalPendiente = totalPendiente,
            productosVendidos = productosVendidos,
            productosDevueltos = productosDevueltos
        )
    }
}