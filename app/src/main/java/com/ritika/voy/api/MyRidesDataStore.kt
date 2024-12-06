package com.ritika.voy.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritika.voy.api.dataclasses.MyRideItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MyRidesDataStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_rides")
        private val MY_RIDES_KEY = stringPreferencesKey("my_rides")
    }

    suspend fun saveRides(rides: List<MyRideItem>) {
        val json = Gson().toJson(rides)
        context.dataStore.edit { preferences ->
            preferences[MY_RIDES_KEY] = json
        }
    }

    val rides: Flow<List<MyRideItem>> = context.dataStore.data.map { preferences ->
        val json = preferences[MY_RIDES_KEY] ?: "[]"
        val type = object : TypeToken<List<MyRideItem>>() {}.type
        Gson().fromJson(json, type)
    }
}

