package com.thejunglegiant.teslastations.presentation.core

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

sealed class StatusBarMode {

    object Default : StatusBarMode() {

        override fun onFragmentResumed(fragment: Fragment) {
            fragment.activity?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    window.statusBarColor = Color.WHITE
                } else {
                    window.statusBarColor = Color.BLACK
                }
            }
        }
    }

    class Colored(@ColorRes val color: Int) : StatusBarMode() {
        override fun onFragmentResumed(fragment: Fragment) {
            fragment.activity?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    window.statusBarColor = ContextCompat.getColor(this, color)
                } else {
                    window.statusBarColor = Color.BLACK
                }
            }
        }
    }

    object Translucent : StatusBarMode() {

        override fun onFragmentResumed(fragment: Fragment) {

            fragment.activity?.apply {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
                window.statusBarColor = Color.TRANSPARENT

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val decor = window.decorView
                    decor.systemUiVisibility =
                        decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }

        }
    }

    abstract fun onFragmentResumed(fragment: Fragment)

    open fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams: WindowManager.LayoutParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }
}