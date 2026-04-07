package com.example.repartidor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.model.InventarioItem
import com.example.repartidor.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val repository: InventarioRepository
) : ViewModel() {

    var inventario by mutableStateOf<List<InventarioItem>>(emptyList())
        private set

    fun cargarInventario(username: String) {
        viewModelScope.launch {
            inventario = repository.obtenerInventario(username)
        }
    }
}