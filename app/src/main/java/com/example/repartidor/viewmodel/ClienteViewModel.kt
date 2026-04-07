package com.example.repartidor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.repository.ClienteRepository
import kotlinx.coroutines.launch

class ClienteViewModel(
    private val repository: ClienteRepository
) : ViewModel() {

    var cliente by mutableStateOf<ClienteEntity?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun buscarCliente(id: Int) {
        viewModelScope.launch {
            val resultado = repository.obtenerClientePorId(id)

            if (resultado != null) {
                cliente = resultado
                error = null
            } else {
                cliente = null
                error = "Cliente no encontrado"
            }
        }
    }

    fun limpiarBusqueda() {
        cliente = null
        error = null
    }
}