package com.thejunglegiant.teslastations.presentation.splash.models

sealed class SplashViewState {
    object Success : SplashViewState()
    object Loading : SplashViewState()
    object Error : SplashViewState()
}
