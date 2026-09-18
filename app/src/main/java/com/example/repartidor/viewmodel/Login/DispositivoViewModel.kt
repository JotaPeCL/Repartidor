package com.example.repartidor.viewmodel.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.repository.DispositivoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DispositivoViewModel(
    private val repository: DispositivoRepository
) : ViewModel() {
    private val _isLoading= MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun activarDispositivo(
        codigo: String,
        credencial: String
    ){
        viewModelScope.launch {
            _isLoading.value=true
            _success.value=false
            _error.value=null

            try{
                val response=repository.activarDispositivo(
                    codigo=codigo,
                    credencial=credencial
                )
                if(response.success){
                    _success.value=true
                }else{
                    _error.value=response.message
                }

            }catch (e: Exception){
                _error.value=e.message?:"Error al activar el dispositivo"
            }finally {
                _isLoading.value=false
            }
        }
    }

    fun limpiarError(){
        _error.value=null
    }

    fun limpiarSuccess(){
        _success.value=false
    }
}