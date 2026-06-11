package com.example.repartidor.data.repository

import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.model.dclass.VentaDetalleCompleta

class AbonosFormRepository(
    private val ventaDao: VentaDao
) {

    suspend fun obtenerDetalleVenta(ventaId: Int): VentaDetalleCompleta {

        val info = ventaDao.getVentaInfo(ventaId)
        val productos = ventaDao.getProductosVenta(ventaId)

        return VentaDetalleCompleta(
            info = info,
            productos = productos
        )
    }
}