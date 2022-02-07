package com.thejunglegiant.teslastations.extensions

import android.content.res.Resources
import android.util.TypedValue

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

fun Int.toPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()