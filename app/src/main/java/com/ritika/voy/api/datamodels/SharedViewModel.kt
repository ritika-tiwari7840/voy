package com.ritika.voy.api.datamodels

import androidx.lifecycle.ViewModel
import com.ritika.voy.api.dataclasses.UserXX

class SharedViewModel : ViewModel() {
    var user: UserXX? = null
}
