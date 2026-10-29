package com.example.repartidor.viewmodel.Login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.remote.RetrofitClient
import com.example.repartidor.data.remote.request.PinAdminRequest
import com.example.repartidor.data.repository.GrupoSync
import com.example.repartidor.data.repository.SyncRepository
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

class SyncAdminViewModel(
    private val repository: SyncRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var pinValidado by mutableStateOf(false)
        private set

    var sincronizacionCompleta by mutableStateOf(false)
        private set

    var errorMensaje by mutableStateOf<String?>(null)
        private set

    var mensajeExito by mutableStateOf<String?>(null)
        private set

    fun verificarPin(pin: String, onSuccess: () -> Unit) {
        if (isLoading) return
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            errorMensaje = "Ingresa un PIN de 6 dígitos."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMensaje = null
            mensajeExito = null
            pinValidado = false

            try {
                val response = RetrofitClient.api.verificarPinAdmin(PinAdminRequest(pin))

                if (response.isSuccessful && response.body()?.success == true) {
                    pinValidado = true
                    onSuccess()
                } else {
                    errorMensaje = when (response.code()) {
                        400 -> "El PIN debe contener 6 dígitos."
                        401 -> "PIN incorrecto o credenciales no válidas."
                        403 -> "El dispositivo no está autorizado."
                        429 -> "Demasiados intentos. Intenta nuevamente más tarde."
                        503 -> "El PIN administrativo no está configurado en Django."
                        else -> "No se pudo validar el PIN. Error HTTP ${response.code()}."
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMensaje = obtenerError(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun sincronizar(grupos: Set<GrupoSync>, onFinish: () -> Unit) {
        if (isLoading) return

        if (!pinValidado) {
            errorMensaje = "Primero debes validar el PIN administrativo."
            return
        }

        if (grupos.isEmpty()) {
            errorMensaje = "Selecciona al menos un grupo."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMensaje = null
            mensajeExito = null
            sincronizacionCompleta = false

            try {
                repository.sicronizarAdmin(grupos)
                sincronizacionCompleta = true
                mensajeExito = "Sincronización administrativa completada correctamente."
                onFinish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMensaje = obtenerError(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun limpiarError() {
        errorMensaje = null
    }

    fun cerrarAcceso() {
        if (isLoading) return
        pinValidado = false
        sincronizacionCompleta = false
        errorMensaje = null
        mensajeExito = null
    }

    private fun obtenerError(e: Exception): String = when {
        e.message?.startsWith("HTTP_403") == true ->
            "El dispositivo se encuentra desactivado."
        e.message?.startsWith("HTTP_401") == true ->
            "Las credenciales del dispositivo no son válidas."
        e.message?.startsWith("HTTP_404") == true ->
            "No se encontró el recurso solicitado."
        e.message?.startsWith("HTTP_500") == true ->
            "El servidor presentó un error."
        e is UnknownHostException || e is ConnectException || e is SocketTimeoutException ->
            "No se pudo conectar con el servidor. Verifica tu conexión."
        else ->
            "No se pudo completar la operación. Inténtalo nuevamente."
    }
}