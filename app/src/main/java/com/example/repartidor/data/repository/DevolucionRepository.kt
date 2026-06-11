package com.example.repartidor.data.repository

import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.model.dclass.ProductoConStock
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import kotlinx.coroutines.flow.Flow

class DevolucionRepository(
    private val miniBodegaDao: MiniBodegaDetalleDao
) {

    fun getProductos(miniBodegaId: Int): Flow<List<ProductoTerminadoEntity>> {
        return miniBodegaDao.getProductosConStock(miniBodegaId)
    }

    fun getVariaciones(
        productoId: Int,
        miniBodegaId: Int
    ): Flow<List<ProductoConStock>> {
        return miniBodegaDao.getVariacionesConStock(productoId, miniBodegaId)
    }
}