package com.thejunglegiant.teslastations.presentation.core.adapters

import android.view.View

interface BaseAdapterCallback<T> {
    fun onItemClick(model: T, view: View)
    fun onLongClick(model: T, view: View): Boolean = false
}