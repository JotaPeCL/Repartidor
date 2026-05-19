package com.example.repartidor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.model.VentaCredito
import com.example.repartidor.data.repository.AbonosRepository
import kotlinx.coroutines.launch

class AbonosViewModel(
    private val repository: AbonosRepository
) : ViewModel() {

    var ventas by mutableStateOf<List<VentaCredito>>(emptyList())
        private set

    var busqueda by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set

    val ventasFiltradas: List<VentaCredito>
        get() {
            if (busqueda.isBlank()) return ventas

            return ventas.filter {
                (it.nombre ?: "").contains(busqueda, true) ||
                        (it.nombreNegocio ?: "").contains(busqueda, true)
            }
        }

    fun cargarVentas() {
        viewModelScope.launch {
            isLoading = true   // 🔄 empieza carga

            ventas = repository.getVentasPendientes()

            isLoading = false  // ✅ termina carga
        }
    }

    fun onBuscarChange(texto: String) {
        busqueda = texto
    }
}