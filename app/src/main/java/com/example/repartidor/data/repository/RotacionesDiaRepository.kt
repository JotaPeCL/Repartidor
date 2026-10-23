package com.example.repartidor.data.repository

import com.example.repartidor.data.local.RotacionDao
import com.example.repartidor.data.local.RotacionDetalleDao
import com.example.repartidor.data.local.RotacionSustitucionDao
import com.example.repartidor.data.model.entity.RotacionEntity
import com.example.repartidor.data.model.entity.RotacionDetalleEntity
import com.example.repartidor.data.model.entity.RotacionSustitucionEntity
import java.util.Calendar

class RotacionesDiaRepository(
    private val rotacionDao: RotacionDao,
    private val rotacionDetalleDao: RotacionDetalleDao,
    private val rotacionSustitucionDao: RotacionSustitucionDao
) {

    // =========================================================
    // OBTENER ROTACIONES DEL DÍA
    // =========================================================

    suspend fun getRotacionesDelDia(
        usuarioId: Int
    ): List<RotacionEntity> {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val inicioDia = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)

        val finDia = calendar.timeInMillis

        return rotacionDao.getRotacionesDelDia(
            inicio = inicioDia,
            fin = finDia,
            usuarioId = usuarioId
        )
    }

    // =========================================================
    // OBTENER ROTACIÓN POR ID
    // =========================================================

    suspend fun getRotacionById(
        rotacionId: Int
    ): RotacionEntity? {

        return rotacionDao.getById(rotacionId)
    }

    // =========================================================
    // OBTENER PRODUCTOS RECIBIDOS
    // =========================================================

    suspend fun getProductosRecibidos(
        rotacionId: Int
    ): List<RotacionDetalleEntity> {

        return rotacionDetalleDao.obtenerPorRotacion(
            rotacionId
        )
    }

    // =========================================================
    // OBTENER PRODUCTOS ENTREGADOS
    // =========================================================

    suspend fun getProductosEntregados(
        rotacionId: Int
    ): List<RotacionSustitucionEntity> {

        return rotacionSustitucionDao.obtenerPorRotacion(
            rotacionId
        )
    }
}