package com.example.repartidor.data.repository

import com.example.repartidor.data.local.VentaDao
import com.example.repartidor.data.model.dclass.VentaCredito

class AbonosRepository(
    private val ventaDao: VentaDao
) {

    suspend fun getVentasPendientes(): List<VentaCredito> {
        return ventaDao.getVentasCreditoPendientes()
    }
}