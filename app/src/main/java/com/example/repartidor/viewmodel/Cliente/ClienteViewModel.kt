package com.example.repartidor.viewmodel.Cliente

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.entity.ClienteEntity
import com.example.repartidor.data.repository.ClienteRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ClienteViewModel(
    private val repository: ClienteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var cliente by mutableStateOf<ClienteEntity?>(null)
        private set

    var resultados by mutableStateOf<List<ClienteEntity>>(emptyList())
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private fun obtenerDiaActual(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "lunes"
            Calendar.TUESDAY -> "martes"
            Calendar.WEDNESDAY -> "miercoles"
            Calendar.THURSDAY -> "jueves"
            Calendar.FRIDAY -> "viernes"
            Calendar.SATURDAY -> "sabado"
            Calendar.SUNDAY -> "domingo"
            else -> ""
        }
    }

    fun buscarCliente(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {

            val userId = sessionManager.getUserId() ?: return@launch

            val diaActual = obtenerDiaActual()

            val res = repository.buscarClientes(
                query = query.trim(),
                userId = userId,
                diaSemana = diaActual
            )

            when {
                res.isEmpty() -> {
                    cliente = null
                    resultados = emptyList()
                    error = "Cliente no encontrado para hoy"
                }

                res.size == 1 -> {
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