package com.thejunglegiant.teslastations.extensions

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.IOException

fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

fun Context.delayedAction(delay: Long, onDelay: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({ onDelay.invoke() }, delay)
}