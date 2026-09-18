package com.example.repartidor.data.remote

import android.content.Context
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.utils.SecureStorage
import kotlinx.coroutines.runBlocking

class DispositivoCredentials(
    context: Context
) {
    private val sessionManager = SessionManager(context)
    private val secureStorage = SecureStorage(context)

    fun getCodigo(): String? {
        return runBlocking {
            sessionManager.getCodigoDispositivo()
        }
    }

    fun getCredencial(): String? {
        return secureStorage.getCredential()
    }
}