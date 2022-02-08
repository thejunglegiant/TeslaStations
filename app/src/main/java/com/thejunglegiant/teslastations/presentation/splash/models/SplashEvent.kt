package com.thejunglegiant.teslastations.presentation.splash.models

sealed class SplashEvent {
    object EnterScreen : SplashEvent()
}