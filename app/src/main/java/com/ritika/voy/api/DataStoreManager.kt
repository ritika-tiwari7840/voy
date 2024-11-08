package com.ritika.voy.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {
    private val Context.dataStore by preferencesDataStore(name = "tokens")

    suspend fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val accessTokenKey = stringPreferencesKey("access")
        val refreshTokenKey = stringPreferencesKey("refresh")
        context.dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
        }
    }

    fun getToken(context: Context, key: String): Flow<String?> {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[dataStoreKey]
        }
    }

    suspend fun clearTokens(context: Context) {
        val accessTokenKey = stringPreferencesKey("access")
        val refreshTokenKey = stringPreferencesKey("refresh")
        context.dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }
}