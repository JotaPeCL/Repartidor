package com.example.repartidor.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartidor.viewmodel.Inventario.InventarioViewModel

class InventarioViewModelFactory(
    private val repository: InventarioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InventarioViewModel(repository) as T
    }
}