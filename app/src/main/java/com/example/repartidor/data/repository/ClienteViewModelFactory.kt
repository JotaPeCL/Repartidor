package com.example.repartidor.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.Cliente.ClienteViewModel

class ClienteViewModelFactory(
    private val repository: ClienteRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClienteViewModel(repository,sessionManager) as T
    }
}