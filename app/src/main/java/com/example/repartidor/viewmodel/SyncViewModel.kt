package com.example.repartidor.viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.preferences.SyncPreferences
import com.example.repartidor.data.repository.SyncRepository
import com.example.repartidor.utils.AppConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class SyncViewModel(
    private val repository: SyncRepository,
    private val sessionManager: SessionManager // 🔥 agregar
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMensaje by mutableStateOf<String?>(null)
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun sincronizar(onFinish: () -> Unit) {
        viewModelScope.launch {

            isLoading = true
            errorMensaje = null

            try {

                val lastSync = sessionManager.lastSyncFlow.first()

                if (shouldSync(lastSync)) {

                    repository.sincronizarTodo()

                    val today = LocalDate.now().toString()
                    sessionManager.saveLastSync(today)
                }

                onFinish()

            } catch (e: Exception) {

                errorMensaje = "No se pudo sincronizar. Verifica tu conexión."

            } finally {
                isLoading = false
            }
        }
    }

    fun limpiarError() {
        errorMensaje = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shouldSync(lastSync: String?): Boolean {

        if (AppConfig.FORCE_SYNC) return true

        if (lastSync == null) return true

        val now = LocalDateTime.now()

        // 🔥 Hora límite (7 AM)
        val todayReset = now.toLocalDate().atTime(7, 0)

        // Si aún no son las 7 AM, el "día" sigue siendo el de ayer
        val effectiveToday = if (now.isBefore(todayReset)) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }

        val lastSyncDate = LocalDate.parse(lastSync)

        return lastSyncDate != effectiveToday
    }
}