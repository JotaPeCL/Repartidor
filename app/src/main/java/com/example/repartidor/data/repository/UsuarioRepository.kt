package com.example.repartidor.data.repository

import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.UsuarioEntity

class UsuarioRepository(private val db: AppDatabase) {

    suspend fun login(username: String): UsuarioEntity? {
        return db.usuarioDao().getByUsername(username)
    }
}