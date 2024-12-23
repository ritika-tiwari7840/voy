package com.ritika.voy.api.dataclasses

import kotlinx.parcelize.Parcelize

@Parcelize
data class EndPoint(
    val coordinates: List<Double>,
    val type: String
): android.os.Parcelable