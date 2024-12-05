package com.ritika.voy.api.datamodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ritika.voy.api.dataclasses.MyRideItem
import com.ritika.voy.api.dataclasses.UserXX

class SharedViewModel : ViewModel() {
    var user: UserXX? = null
    private val _rideItem = MutableLiveData<List<MyRideItem>>(emptyList())
    val rideItem: LiveData<List<MyRideItem>> = _rideItem

    fun addRideItems(newRides: List<MyRideItem>) {
        val currentRides = _rideItem.value ?: emptyList()
        val updatedRides = (currentRides + newRides)
            .distinctBy { it.id }
            .sortedByDescending { it.startTime }
        _rideItem.value = updatedRides
    }

    fun clearRides() {
        _rideItem.value = emptyList()
    }
}