package com.example.repartidor.data.repository

import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.ProductoTerminadoDao
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.model.ProductoConStock
import com.example.repartidor.data.model.ProductoTerminadoEntity
import kotlinx.coroutines.flow.Flow

class VentaRepository(
    private val productoDao: ProductoTerminadoDao,
    private val miniBodegaDao: MiniBodegaDetalleDao,
    private val clienteDao: ClienteDao
) {

    // 🔹 PRODUCTOS
    fun getProductos(miniBodegaId: Int): Flow<List<ProductoTerminadoEntity>> {
        return miniBodegaDao.getProductosConStock(miniBodegaId)
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
    suspend fun getClienteById(id: Int): ClienteEntity? {
        return clienteDao.getClienteById(id)
    }

}