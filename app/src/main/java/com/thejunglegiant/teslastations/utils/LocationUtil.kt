package com.thejunglegiant.teslastations.utils

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest

object LocationUtil {

    fun createLocationRequest(): LocationRequest =
        LocationRequest.create().also {
            it.interval = 3000
            it.fastestInterval = 1500
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

}