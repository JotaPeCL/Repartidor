package com.example.repartidor.data.repository

import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.model.ClienteEntity

class ClienteRepository(
    private val clienteDao: ClienteDao
) {

    suspend fun obtenerClientePorId(id: Int): ClienteEntity? {
        return clienteDao.obtenerClientePorId(id)
    }
}