package com.example.repartidor.data.repository

import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.HomeData
import com.example.repartidor.data.model.RutaEntity
import com.example.repartidor.data.model.UsuarioEntity
import com.example.repartidor.data.model.VehiculoEntity

class HomeRepository(private val db: AppDatabase) {

    suspend fun obtenerDatosPorUsername(username: String): HomeData {

        val usuario = db.usuarioDao().getByUsername(username)

        val ruta = usuario?.id?.let {
            db.rutaDao().getByUsuarioId(it)
        }

        val vehiculo = ruta?.vehiculoId?.let {
            db.vehiculoDao().getById(it)
        }

        return HomeData(usuario, ruta, vehiculo)
    }
}