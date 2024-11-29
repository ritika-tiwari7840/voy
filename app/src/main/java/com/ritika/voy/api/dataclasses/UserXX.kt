package com.ritika.voy.api.dataclasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class UserXX(
    val created_at: String,
    val email: String,
    val email_verified: Boolean,
    val emergency_contact_phone: String,
    val first_name: String,
    val full_name: String,
    val gender: String,
    val id: Int,
    val last_name: String,
    val phone_number: String,
    val phone_verified: Boolean,
    val profile_photo: String?,
    val is_driver_verified: Boolean,
    val rating_as_driver: Double,
    val rating_as_passenger: Double
) : Parcelable
