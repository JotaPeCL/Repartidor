package com.example.repartidor.data.repository

import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.MiniBodegaEntity

class MiniBodegaRepository(private val db: AppDatabase) {

    suspend fun getMiniBodegaByUsuario(userId: Int): MiniBodegaEntity? {
        return db.miniBodegaDao().getByUsuario(userId)
    }
}