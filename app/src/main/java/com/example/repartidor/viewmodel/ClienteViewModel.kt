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

    var resultados by mutableStateOf<List<ClienteEntity>>(emptyList())
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun buscarCliente(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            val res = repository.buscarClientes(query.trim())

            when {
                res.isEmpty() -> {
                    cliente = null
                    resultados = emptyList()
                    error = "Cliente no encontrado"
                }
                res.size == 1 -> {
                    // Seleccionamos automáticamente y también guardamos en resultados
                    // para que la tarjeta permanezca visible en la pantalla
                    cliente = res.first()
                    resultados = res
                    error = null
                }
                else -> {
                    cliente = null
                    resultados = res
                    error = null
                }
            }
        }
    }

    fun seleccionarCliente(clienteSeleccionado: ClienteEntity) {
        cliente = clienteSeleccionado
        // Ya NO limpiamos los resultados aquí para que la lista permanezca visible
        error = null
    }

    fun limpiarBusqueda() {
        cliente = null
        resultados = emptyList()
        error = null
    }

    fun limpiarClienteSeleccionado() {
        cliente = null
        // Opcional: Si limpias el cliente seleccionado, ¿quieres que desaparezca la lista?
        // Si quieres conservar la última búsqueda, elimina la siguiente línea:
        // resultados = emptyList()
        error = null
    }
}