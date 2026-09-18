package com.example.repartidor.data.repository

import com.example.repartidor.data.remote.RetrofitClient
import com.example.repartidor.data.remote.request.DispositivoActivacionRequest
import com.example.repartidor.data.remote.request.DispositivoActivacionResponse

class DispositivoRepository {
    suspend fun activarDispositivo(
        codigo: String,
        credencial: String
    ): DispositivoActivacionResponse {
        val request = DispositivoActivacionRequest(
            codigo = codigo,
            credencial = credencial
        )

        val response = RetrofitClient.api.activarDispositivo(request)

        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía del servidor")
        } else {
            throw Exception(
                response.errorBody()?.string()?: "Error al activar el dispositivo"
            )
        }
    }
}