package com.example.repartidor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.UsuarioEntity
import com.example.repartidor.data.repository.MiniBodegaRepository
import com.example.repartidor.data.repository.UsuarioRepository
import kotlinx.coroutines.launch

class LoginViewModel(

    private val repository: UsuarioRepository,
    private val miniBodegaRepository: MiniBodegaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    var loginState by mutableStateOf<UsuarioEntity?>(null)
    var error by mutableStateOf<String?>(null)
        private set

    fun login(username: String) {
        viewModelScope.launch {
            val user = repository.login(username)

            if (user != null) {
                sessionManager.saveUser(user.username)

                val miniBodega = miniBodegaRepository
                    .getMiniBodegaByUsuario(user.id)

                if (miniBodega != null) {

                    // 🔥 GUARDAR CAMIONETA
                    sessionManager.saveMiniBodegaId(miniBodega.id)

                } else {
                    error = "El usuario no tiene mini bodega asignada"
                    return@launch
                }

                loginState = user
                error = null
            } else {
                error = "Usuario no encontrado"
            }
        }
    }

    fun logout() {
        loginState = null
        error = null
    }
}