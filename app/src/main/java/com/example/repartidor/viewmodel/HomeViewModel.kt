package com.example.repartidor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.model.HomeData
import com.example.repartidor.data.model.RutaEntity
import com.example.repartidor.data.model.UsuarioEntity
import com.example.repartidor.data.model.VehiculoEntity
import com.example.repartidor.data.repository.HomeRepository
import kotlinx.coroutines.launch



class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    var homeData by mutableStateOf(HomeData(null, null, null))
        private set

    fun cargarDatosPorUsername(username: String) {
        viewModelScope.launch {
            homeData = repository.obtenerDatosPorUsername(username)
        }
    }
}