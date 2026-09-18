package com.example.repartidor.data.remote

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class DispositivoAuthInterceptor(
    context: Context
) : Interceptor {

    private val credentials = DispositivoCredentials(context)

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        // La activación no necesita autenticación previa.
        if (request.url.encodedPath.endsWith("/dispositivos/activar/")) {
            return chain.proceed(request)
        }

        val codigo = credentials.getCodigo()
        val credencial = credentials.getCredencial()

        // Si todavía no hay credenciales guardadas,
        // dejamos pasar la petición sin encabezados.
        if (codigo.isNullOrBlank() || credencial.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val requestConAuth = request.newBuilder()
            .addHeader("X-Dispositivo", codigo)
            .addHeader("X-Credencial", credencial)
            .build()

        return chain.proceed(requestConAuth)
    }
}