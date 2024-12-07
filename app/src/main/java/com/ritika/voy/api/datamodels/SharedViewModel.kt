package com.ritika.voy.api.datamodels


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ritika.voy.api.MyRidesDataStore
import com.ritika.voy.api.dataclasses.MyRideItem
import com.ritika.voy.api.dataclasses.UserXX
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    var user: UserXX? = null
    var rideId:Int = 0

    private val dataStore = MyRidesDataStore(application)
    private val _rideItem = MutableLiveData<List<MyRideItem>>(emptyList())
    val rideItem: LiveData<List<MyRideItem>> = _rideItem

    init {
        viewModelScope.launch {
            _rideItem.value = dataStore.rides.first()
        }
    }
    fun addRideItems(newRides: List<MyRideItem>) {
        val currentRides = _rideItem.value ?: emptyList()
        val updatedRides = (currentRides + newRides)
            .distinctBy { it.id }
            .sortedByDescending { it.startTime }
        _rideItem.value = updatedRides
        saveRidesToDataStore(updatedRides)
    }

    fun updateRidesFromResponse(newRides: List<MyRideItem>) {
        val updatedRides = newRides.sortedByDescending { it.startTime }
        _rideItem.value = updatedRides
        saveRidesToDataStore(updatedRides)
    }

    fun clearRides() {
        _rideItem.value = emptyList()
        saveRidesToDataStore(emptyList())
    }

    private fun saveRidesToDataStore(rides: List<MyRideItem>) {
        viewModelScope.launch {
            dataStore.saveRides(rides)
        }
    }
}

