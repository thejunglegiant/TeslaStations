package com.thejunglegiant.teslastations.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.presentation.core.EventHandler
import com.thejunglegiant.teslastations.presentation.splash.models.SplashEvent
import com.thejunglegiant.teslastations.presentation.splash.models.SplashViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashViewModel(
    private val repository: IStationsRepository
) : ViewModel(), EventHandler<SplashEvent> {

    private val _viewState = MutableLiveData<SplashViewState>(SplashViewState.Loading)
    val viewState: LiveData<SplashViewState> = _viewState

    override fun obtainEvent(event: SplashEvent) {
        when (val currentViewState = _viewState.value) {
            is SplashViewState.Error -> reduce(event, currentViewState)
            is SplashViewState.Loading -> reduce(event, currentViewState)
            is SplashViewState.Success -> reduce(event, currentViewState)
        }
    }

    private fun reduce(event: SplashEvent, currentState: SplashViewState.Error) {
        when (event) {
            SplashEvent.EnterScreen -> writeStationsIfNeeded(needReload = true)
        }
    }

    private fun reduce(event: SplashEvent, currentState: SplashViewState.Loading) {
        when (event) {
            SplashEvent.EnterScreen -> writeStationsIfNeeded()
        }
    }

    private fun reduce(event: SplashEvent, currentState: SplashViewState.Success) {
        when (event) {
            SplashEvent.EnterScreen -> writeStationsIfNeeded(needReload = true)
        }
    }

    private fun writeStationsIfNeeded(needReload: Boolean = false) {
        if (needReload) _viewState.postValue(SplashViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.initDb()
            _viewState.postValue(
                if (result) {
                    SplashViewState.Success
                } else {
                    SplashViewState.Error
                }
            )
        }
    }
}