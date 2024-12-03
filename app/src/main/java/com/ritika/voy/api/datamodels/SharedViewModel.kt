package com.ritika.voy.api.datamodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ritika.voy.api.dataclasses.DataXXX
import com.ritika.voy.api.dataclasses.MyRideItem
import com.ritika.voy.api.dataclasses.OfferRideResponse
import com.ritika.voy.api.dataclasses.UserXX

class SharedViewModel : ViewModel() {
    var user: UserXX? = null
    private val _rideItem = MutableLiveData<List<MyRideItem>>()
    val rideItem: LiveData<List<MyRideItem>> = _rideItem

    fun setRideItem(rides: List<MyRideItem>) {
        _rideItem.value = rides
    }
}
