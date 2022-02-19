package com.thejunglegiant.teslastations.presentation.core

interface ViewStateHandler<T> {
    fun render(state: T)
}