package com.thejunglegiant.teslastations.presentation.core

interface EventHandler<T> {
    fun obtainEvent(event: T)
}