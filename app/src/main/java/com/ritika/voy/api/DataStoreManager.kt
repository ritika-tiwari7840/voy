package com.ritika.voy.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {
    private val Context.dataStore by preferencesDataStore(name = "tokens")
    private val Context.dataStore1 by preferencesDataStore(name = "userData")

    suspend fun SaveUserData(
        context: Context,
        id: String,
        email: String,
        firstName: String,
        lastName: String,
        fullName: String,
        createdAt: String,
        phoneNo: String,
        gender: String,
        emergencyContact: String,
        profilePhoto: String,
        ratingAsDriver: String,
        ratingAsPassenger: String,
    ) {
        val idKey = stringPreferencesKey("id")
        val emailKey = stringPreferencesKey("email")
        val firstNameKey = stringPreferencesKey("firstName")
        val lastNameKey = stringPreferencesKey("lastName")
        val fullNameKey = stringPreferencesKey("fullName")
        val createdAtKey = stringPreferencesKey("createdAt")
        val phoneKey = stringPreferencesKey("phoneNo")
        val genderKey = stringPreferencesKey("gender")
        val emergencyContactKey = stringPreferencesKey("emergencyContact")
        val profileKey = stringPreferencesKey("profilePhoto")
        val ratingAsDriverKey = stringPreferencesKey("ratingAsDriver")
        val ratingAsPassengerKey = stringPreferencesKey("ratingAsPassenger")
        context.dataStore1.edit { preferences ->
            preferences[idKey] = id
            preferences[emailKey] = email
            preferences[firstNameKey] = firstName
            preferences[lastNameKey] = lastName
            preferences[fullNameKey] = fullName
            preferences[createdAtKey] = createdAt
            preferences[phoneKey] = phoneNo
            preferences[genderKey] = gender
            preferences[emergencyContactKey] = emergencyContact
            preferences[profileKey] = profilePhoto.toString()
            preferences[ratingAsDriverKey] = ratingAsDriver
            preferences[ratingAsPassengerKey] = ratingAsPassenger
        }
    }

    fun getUserData(context: Context, key: String): Flow<String?> {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore1.data.map { preferences ->
            preferences[dataStoreKey]
        }
    }

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