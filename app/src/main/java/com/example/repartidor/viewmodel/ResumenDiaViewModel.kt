package com.example.repartidor.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.InventarioItem
import com.example.repartidor.data.model.ResumenDiaState
import com.example.repartidor.data.repository.InventarioRepository
import com.example.repartidor.data.repository.ResumenDiaRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class ResumenDiaViewModel(
    private val repository: ResumenDiaRepository,
    private val sessionManager: SessionManager,
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    var state by mutableStateOf(ResumenDiaState())
        private set

    var isLoading by mutableStateOf(false)
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarResumen() {
        viewModelScope.launch {

            isLoading = true

            val usuarioId = sessionManager.getUserId() ?: return@launch

            val inicioDia = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val finDia = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            Log.d("RESUMEN", "Inicio: $inicioDia")
            Log.d("RESUMEN", "Fin: $finDia")

            state = repository.obtenerResumen(
                inicioDia = inicioDia,
                finDia = finDia,
                usuarioId = usuarioId
            )

            isLoading = false
        }
    }

    suspend fun obtenerInventarioParaImpresion(): List<InventarioItem> {

        val username = sessionManager.getUser() ?: return emptyList()

        return inventarioRepository.obtenerInventario(username)
    }
}