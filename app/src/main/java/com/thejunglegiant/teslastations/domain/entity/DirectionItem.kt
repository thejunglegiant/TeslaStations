package com.thejunglegiant.teslastations.domain.entity

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

data class DirectionItem(
    val start: String = "Your location",
    val destination: String,
    val bounds: LatLngBounds,
    val points: List<LatLng>
)
