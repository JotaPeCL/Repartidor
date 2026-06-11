package com.example.repartidor.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.Login.LoginViewModel

class LoginViewModelFactory(
    private val usuarioRepository: UsuarioRepository,
    private val miniBodegaRepository: MiniBodegaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(
            usuarioRepository,
            miniBodegaRepository,
            sessionManager
        ) as T
    }
}