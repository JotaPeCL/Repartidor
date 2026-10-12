package com.example.repartidor.data.repository

import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.model.dclass.ProductoConStock
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import kotlinx.coroutines.flow.Flow

class RotacionRepository(
    private val miniBodegaDao: MiniBodegaDetalleDao
) {

    fun getProductos(
        miniBodegaId: Int
    ): Flow<List<ProductoTerminadoEntity>> {
        return miniBodegaDao.getTodosProductosParaRotacion()
    }

    fun getVariaciones(
        productoId: Int,
        miniBodegaId: Int
    ): Flow<List<ProductoConStock>> {
        return miniBodegaDao.getTodasVariacionesParaRotacion(
            productoId,
            miniBodegaId
        )
    }

    fun getVariacionesParaSustitucion(
        miniBodegaId: Int
    ): Flow<List<ProductoSustitucion>> {
        return miniBodegaDao.getVariacionesParaRotacion(miniBodegaId)
    }
}