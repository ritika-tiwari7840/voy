package com.ritika.voy.api.dataclasses

import kotlinx.parcelize.Parcelize


@Parcelize
data class StartPoint(
    val coordinates: List<Double>,
    val type: String,
) : android.os.Parcelable