package com.example.repartidor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.ReabastecimientoItem
import com.example.repartidor.data.remote.PedidoDetalleRequest
import com.example.repartidor.data.remote.PedidoReabastecimientoRequest
import com.example.repartidor.data.remote.RetrofitClient
import com.example.repartidor.data.repository.HomeRepository
import kotlinx.coroutines.launch

class ReabastecimientoProcesoViewModel(
    private val sessionManager: SessionManager,
    private val homeRepository: HomeRepository
) : ViewModel() {

    fun enviarPedido(
        items: List<ReabastecimientoItem>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {

            try {
                // 🔹 Obtener username
                val username = sessionManager.getUser()

                if (username == null) {
                    onError("Usuario no encontrado")
                    return@launch
                }

                // 🔹 Obtener ruta_id desde Room
                val rutaId = homeRepository.getRutaId(username)

                if (rutaId == null) {
                    onError("Ruta no encontrada")
                    return@launch
                }

                // 🔹 Mapear productos
                val productos = items.map {
                    PedidoDetalleRequest(
                        producto_variacion_id = it.productoVariacionId,
                        cantidad = it.cantidad
                    )
                }

                // 🔹 Crear request
                val request = PedidoReabastecimientoRequest(
                    ruta_id = rutaId,
                    productos = productos
                )

                // 🔥 Llamada a API
                val response = RetrofitClient.api.crearReabastecimiento(request)

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Error en la petición: ${response.code()}")
                }

            } catch (e: Exception) {
                onError(e.message ?: "Error desconocido")
            }
        }
    }
}