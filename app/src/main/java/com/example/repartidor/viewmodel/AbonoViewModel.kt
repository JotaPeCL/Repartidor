package com.example.repartidor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.repository.AbonoRepository
import kotlinx.coroutines.launch

class AbonoViewModel(
    private val repository: AbonoRepository
) : ViewModel() {

    var abonoResult by mutableStateOf<AbonoResult>(AbonoResult.Idle)
        private set

    fun registrarAbono(
        ventaId: Int,
        monto: Double,
        usuarioId: Int
    ) {
        viewModelScope.launch {
            try {
                repository.registrarAbono(ventaId, monto, usuarioId)

                abonoResult = AbonoResult.Success

            } catch (e: Exception) {
                abonoResult = AbonoResult.Error(
                    e.message ?: "Error al registrar abono"
                )
            }
        }
    }

    fun resetResult() {
        abonoResult = AbonoResult.Idle
    }
}
sealed class AbonoResult {
    object Idle : AbonoResult()
    object Success : AbonoResult()
    data class Error(val message: String) : AbonoResult()
}