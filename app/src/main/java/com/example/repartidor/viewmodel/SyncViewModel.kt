package com.example.repartidor.viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun sincronizar(onFinish: () -> Unit) {
        viewModelScope.launch {

            val lastSync = sessionManager.lastSyncFlow.first()

            if (shouldSync(lastSync)) {

                repository.sincronizarTodo()

                val today = LocalDate.now().toString()
                sessionManager.saveLastSync(today)
                onFinish()
            } else {
                println("Ya sincronizado hoy")
            }

             // 🔥 navega al final SIEMPRE
        }
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