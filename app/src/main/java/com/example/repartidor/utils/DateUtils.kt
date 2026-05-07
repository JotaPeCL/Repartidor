package com.example.repartidor.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertirFechaATimestamp(fecha: String): Long {
    return try {
        val formato = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            java.util.Locale.getDefault()
        )
        formato.parse(fecha)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

fun formatearFechaHora(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}