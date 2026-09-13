package com.example.repartidor.data.repository

import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.model.entity.ClienteEntity

class ClienteRepository(
    private val clienteDao: ClienteDao
) {

    suspend fun obtenerClientePorId(id: Int): ClienteEntity? {
        return clienteDao.obtenerClientePorId(id)
    }

    // ── NUEVA FUNCIÓN ─────────────────────────────────────────────
    suspend fun buscarClientes(
        query: String,
        userId: Int,
        diaSemana: String
    ): List<ClienteEntity> {
        return clienteDao.buscarPorIdNombreYDia(
            query = query,
            userId = userId,
            diaSemana = diaSemana
        )
    }
}