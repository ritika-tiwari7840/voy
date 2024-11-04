package com.ritika.voy.api.datamodels

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "user_data")

class UserData private constructor(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_FULL_NAME_KEY = stringPreferencesKey("user_full_name")
        private val CREATED_AT_KEY = stringPreferencesKey("created_at")

        @Volatile
        private var INSTANCE: UserData? = null

        fun getInstance(context: Context): UserData {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserData(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val userFullName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_FULL_NAME_KEY]
    }

    val createdAt: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CREATED_AT_KEY]
    }

    // Function to save the verification response and tokens
    suspend fun saveVerificationResponse(
        accessToken: String,
        refreshToken: String,
        userId: String,
        email: String,
        fullName: String,
        createdAt: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_FULL_NAME_KEY] = fullName
            preferences[CREATED_AT_KEY] = createdAt
        }
    }

    // Function to get access token synchronously
    suspend fun getAccessToken(): String? {
        return accessToken.firstOrNull()  // Use the Flow directly
    }

    // Function to clear stored data (for logout or token refresh)
    suspend fun clearData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
