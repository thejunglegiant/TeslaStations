package com.thejunglegiant.teslastations.extensions

import android.util.Log
import com.thejunglegiant.teslastations.BuildConfig

fun Any.logd(text: String) {
    if (BuildConfig.ENABLE_LOGS) {
        Log.d(this::class.java.simpleName, text)
    }
}

fun Any.loge(text: String) {
    if (BuildConfig.ENABLE_LOGS) {
        Log.e(this::class.java.simpleName, text)
    }
}

fun Any.loge(ex: Exception) {
    if (BuildConfig.DEBUG) {
        Log.e(this::class.java.simpleName, ex.message ?: ex.toString())
    }
}