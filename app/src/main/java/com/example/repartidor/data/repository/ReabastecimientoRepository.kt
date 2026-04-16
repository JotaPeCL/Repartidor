package com.example.repartidor.data.repository

import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.ProductoTerminadoDao
import com.example.repartidor.data.local.ProductoVariacionDao
import com.example.repartidor.data.model.ProductoConStock
import com.example.repartidor.data.model.ProductoTerminadoEntity
import kotlinx.coroutines.flow.Flow

class ReabastecimientoRepository(
    private val productoDao: ProductoTerminadoDao,
    private val miniBodegaDetalleDao: MiniBodegaDetalleDao
) {

    fun getProductos(): Flow<List<ProductoTerminadoEntity>> {
        return productoDao.getAllP()
    }

    fun getVariaciones(
        productoId: Int,
        miniBodegaId: Int
    ): Flow<List<ProductoConStock>> {
        return miniBodegaDetalleDao
            .getVariacionesParaReabastecimiento(productoId, miniBodegaId)
    }
}
