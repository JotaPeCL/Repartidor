package com.example.repartidor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.model.DetalleVentaUI
import com.example.repartidor.data.model.VentaUI
import com.example.repartidor.data.repository.VentasDiaRepository
import kotlinx.coroutines.launch

class VentasDiaViewModel(
    private val repository: VentasDiaRepository
) : ViewModel() {

    var ventas by mutableStateOf<List<VentaUI>>(emptyList())
        private set

    var detalleVenta by mutableStateOf<List<DetalleVentaUI>>(emptyList())
        private set

    var ventaSeleccionada by mutableStateOf<VentaUI?>(null)
        private set

    var mostrarDialogo by mutableStateOf(false)
        private set


    fun cargarVentas() {
        viewModelScope.launch {
            ventas = repository.getVentasDelDia()
        }
    }


    fun seleccionarVenta(venta: VentaUI) {
        viewModelScope.launch {
            ventaSeleccionada = venta
            detalleVenta = repository.getDetalleVenta(venta.id)
            mostrarDialogo = true
        }
    }


    fun cerrarDialogo() {
        mostrarDialogo = false
        ventaSeleccionada = null
        detalleVenta = emptyList()
    }
}