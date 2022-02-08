package com.thejunglegiant.teslastations.extensions

import android.content.res.Resources
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun getStatusBarHeight(res: Resources): Int {
    return (24 * res.displayMetrics.density).toInt()
}

fun View.showSnackBar(msg: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar
        .make(this, msg, duration)
        .show()
}

fun View.showSnackBar(@StringRes msg: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar
        .make(this, msg, duration)
        .show()
}

fun View.showSnackBar(
    msg: String,
    actionText: String,
    action: () -> Unit,
    duration: Int = Snackbar.LENGTH_INDEFINITE
) {
    Snackbar
        .make(this, msg, duration)
        .setAction(actionText) {
            action.invoke()
        }
        .show()
}

fun View.showSnackBar(
    @StringRes msg: Int,
    @StringRes actionText: Int,
    duration: Int = Snackbar.LENGTH_INDEFINITE,
    action: () -> Unit
) {
    Snackbar
        .make(this, msg, duration)
        .setAction(actionText) {
            action.invoke()
        }
        .show()
}

fun Fragment.toastL(text: String) {
    Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
}

fun Fragment.toastL(@StringRes text: Int) {
    Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
}

fun Fragment.toastSh(text: String) {
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
}

fun Fragment.toastSh(@StringRes text: Int) {
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
}

fun View?.show() {
    this?.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View?.gone() {
    this?.visibility = View.GONE
}