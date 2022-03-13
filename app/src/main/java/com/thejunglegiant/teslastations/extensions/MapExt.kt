package com.thejunglegiant.teslastations.extensions

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun bitmapFromRes(resources: Resources, @DrawableRes vectorDrawable: Int): BitmapDescriptor? {
    return ResourcesCompat.getDrawable(resources, vectorDrawable, null)?.run {
        BitmapDescriptorFactory.fromBitmap(this.toBitmap())
    }
}