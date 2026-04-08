package com.example.repartidor.data.repository

import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.ProductoTerminadoDao
import com.example.repartidor.data.model.ProductoConStock
import com.example.repartidor.data.model.ProductoTerminadoEntity
import kotlinx.coroutines.flow.Flow

class VentaRepository(
    private val productoDao: ProductoTerminadoDao,
    private val miniBodegaDao: MiniBodegaDetalleDao
) {

    // 🔹 PRODUCTOS
    fun getProductos(): Flow<List<ProductoTerminadoEntity>> {
        return productoDao.getProductos()
    }

    // 🔹 VARIACIONES FILTRADAS POR CAMIONETA
    fun getVariaciones(
        productoId: Int,
        miniBodegaId: Int
    ): Flow<List<ProductoConStock>> {

        return miniBodegaDao.getVariacionesConStock(
            productoId,
            miniBodegaId
        )
    }
}