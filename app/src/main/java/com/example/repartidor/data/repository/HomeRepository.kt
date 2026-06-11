package com.example.repartidor.data.repository

import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.dclass.HomeData
import com.example.repartidor.data.model.entity.RutaEntity
import com.example.repartidor.data.model.entity.UsuarioEntity
import com.example.repartidor.data.model.entity.VehiculoEntity

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

    suspend fun getRutaId(username: String): Int? {

        val usuario = db.usuarioDao().getByUsername(username)

        val ruta = usuario?.id?.let {
            db.rutaDao().getByUsuarioId(it)
        }

        return ruta?.id
    }
}