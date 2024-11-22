package com.ritika.voy.api.dataclasses

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class UpdateUserRequest(
    val profilePhoto: MultipartBody.Part? = null ,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val emergencyContactPhone: String,
)
