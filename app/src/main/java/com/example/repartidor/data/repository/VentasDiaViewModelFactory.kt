package com.example.repartidor.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartidor.viewmodel.VentasDiaViewModel

class VentasDiaViewModelFactory(
    private val repository: VentasDiaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VentasDiaViewModel(repository) as T
    }
}