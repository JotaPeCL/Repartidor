package com.example.repartidor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.repository.MiniBodegaRepository2
import kotlinx.coroutines.launch

class CierreMiniBodegaViewModel(
    private val repository: MiniBodegaRepository2
) : ViewModel() {

    fun cerrarMiniBodega(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {

            val result = repository.cerrarMiniBodega()

            result.onSuccess {
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Error")
            }
        }
    }
}