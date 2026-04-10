package com.example.repartidor.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
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
        val MINIBODEGA_ID_KEY = intPreferencesKey("mini_bodega_id")
        val USER_ID_KEY = intPreferencesKey("user_id")
    }

    // 🔹 Guardar usuario
    suspend fun saveUser(username: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
        }
    }

    suspend fun saveUserId(id: Int) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = id
        }
    }

    // 🔹 GUARDAR MINI BODEGA (CAMIONETA)
    suspend fun saveMiniBodegaId(id: Int) {
        context.dataStore.edit { prefs ->
            prefs[MINIBODEGA_ID_KEY] = id
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


    // 🔹 OBTENER MINI BODEGA
    val miniBodegaFlow: Flow<Int?> = context.dataStore.data
        .catch { e ->
            println("❌ Error en DataStore miniBodega: ${e.message}")
            emit(emptyPreferences())
        }
        .map { prefs ->
            prefs[MINIBODEGA_ID_KEY]
        }

    // 🔹 OBTENER DIRECTO (IMPORTANTE)
    suspend fun getMiniBodegaId(): Int? {
        return context.dataStore.data.first()[MINIBODEGA_ID_KEY]
    }
    suspend fun getUserId(): Int? {
        return context.dataStore.data.first()[USER_ID_KEY]
    }


    // 🔹 Borrar sesión (logout)
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(USERNAME_KEY)
            prefs.remove(MINIBODEGA_ID_KEY)
            prefs.remove(USER_ID_KEY)
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
