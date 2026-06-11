package com.example.repartidor.viewmodel.Abonos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.model.dclass.VentaDetalleCompleta
import com.example.repartidor.data.repository.AbonosFormRepository
import kotlinx.coroutines.launch

class AbonosFormViewModel(
    private val repository: AbonosFormRepository
) : ViewModel() {

    var ventaDetalle by mutableStateOf<VentaDetalleCompleta?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun cargarVenta(ventaId: Int) {
        viewModelScope.launch {
            isLoading = true
            ventaDetalle = repository.obtenerDetalleVenta(ventaId)
            isLoading = false
        }
    }
}