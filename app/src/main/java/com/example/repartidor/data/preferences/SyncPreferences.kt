package com.example.repartidor.data.preferences

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

object SyncPreferences {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_LAST_SYNC = "last_sync_date"

    @RequiresApi(Build.VERSION_CODES.O)
    fun guardarFechaSync(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val hoy = java.time.LocalDate.now().toString()
        prefs.edit().putString(KEY_LAST_SYNC, hoy).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun yaSincronizoHoy(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val ultimaFecha = prefs.getString(KEY_LAST_SYNC, null)
        val hoy = java.time.LocalDate.now().toString()
        return ultimaFecha == hoy
    }
}