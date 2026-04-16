package com.example.repartidor.data.repository

import com.example.repartidor.data.local.MiniBodegaDao
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.remote.CerrarMiniBodegaRequest
import com.example.repartidor.data.remote.MiniBodegaDetalleRequest
import com.example.repartidor.data.remote.RetrofitClient

class MiniBodegaRepository2(
    private val miniBodegaDao: MiniBodegaDao,
    private val detalleDao: MiniBodegaDetalleDao,
    private val sessionManager: SessionManager
) {

    suspend fun cerrarMiniBodega(): Result<Unit> {
        return try {

            // 🔥 1. Obtener ID REAL desde sesión
            val miniBodegaId = sessionManager.getMiniBodegaId()
                ?: return Result.failure(Exception("No hay mini bodega en sesión"))

            // 🔥 2. Obtener detalles SOLO de esa minibodega
            val detalles = detalleDao.obtenerDetallesPorMiniBodega(miniBodegaId)

            if (detalles.isEmpty()) {
                return Result.failure(Exception("No hay productos en la mini bodega"))
            }

            // 🔥 3. Mapear a request
            val productos = detalles.map {
                MiniBodegaDetalleRequest(
                    producto_variacion_id = it.productoVariacionId,
                    cantidad_actual = it.cantidadActual.toInt()
                )
            }

            // 🔥 4. Crear request
            val request = CerrarMiniBodegaRequest(
                mini_bodega_id = miniBodegaId,
                productos = productos
            )

            // 🔥 5. Enviar al backend
            val response = RetrofitClient.api.cerrarMiniBodega(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}