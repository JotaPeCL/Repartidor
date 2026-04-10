package com.example.repartidor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.repository.VentaLocalRepository
import kotlinx.coroutines.launch

class VentaProcesoViewModel(
    private val repository: VentaLocalRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var clienteId: Int? = null
        private set

    fun setCliente(id: Int?) {
        clienteId = id
    }

    fun reset() {
        clienteId = null
    }

    fun confirmarVenta(
        items: List<CarritoItem>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {

                val usuarioId = sessionManager.getUserId()

                if (usuarioId == null) {
                    onError("Usuario no identificado")
                    return@launch
                }

                val miniBodegaId = sessionManager.getMiniBodegaId()

                if (miniBodegaId == null) {
                    onError("No hay mini bodega activa")
                    return@launch
                }

                repository.guardarVenta(
                    clienteId = clienteId,
                    usuarioId = usuarioId,
                    items = items,
                    miniBodegaId = miniBodegaId
                )

                onSuccess()

            } catch (e: Exception) {
                onError(e.message ?: "Error al vender")
            }
        }
    }
}