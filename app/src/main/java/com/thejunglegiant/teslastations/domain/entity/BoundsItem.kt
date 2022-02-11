package com.thejunglegiant.teslastations.domain.entity

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class BoundsItem(val southwest: LatLng, val northeast: LatLng) : Serializable