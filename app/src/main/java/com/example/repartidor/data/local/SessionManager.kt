package com.example.repartidor.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        val USERNAME_KEY = stringPreferencesKey("username")
    }

    // 🔹 Guardar usuario
    suspend fun saveUser(username: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
        }
    }

    // 🔹 Obtener usuario
    val userFlow: Flow<String?> = context.dataStore.data
        .catch { e ->
            println("❌ Error en DataStore: ${e.message}")
            emit(emptyPreferences())
        }
        .map { prefs ->
            prefs[USERNAME_KEY]
        }

    // 🔹 Borrar sesión (logout)
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(USERNAME_KEY)
        }
    }

    val LAST_SYNC_KEY = stringPreferencesKey("last_sync")

    val lastSyncFlow: Flow<String?> = context.dataStore.data
        .catch { e ->
            println("❌ Error en DataStore lastSync: ${e.message}")
            emit(emptyPreferences())
        }
        .map { it[LAST_SYNC_KEY] }

    suspend fun saveLastSync(date: String) {
        context.dataStore.edit {
            it[LAST_SYNC_KEY] = date
        }
    }

    suspend fun getUser(): String? {
        return context.dataStore.data.first()[USERNAME_KEY]
    }

}
