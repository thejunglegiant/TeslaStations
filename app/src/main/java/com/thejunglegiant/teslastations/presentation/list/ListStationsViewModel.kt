package com.thejunglegiant.teslastations.presentation.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.domain.entity.MapSettingsItem
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.presentation.core.EventHandler
import com.thejunglegiant.teslastations.presentation.list.models.ListEvent
import com.thejunglegiant.teslastations.presentation.list.models.ListViewState
import com.thejunglegiant.teslastations.presentation.map.models.MapViewState
import kotlinx.coroutines.launch

class ListStationsViewModel(
    private val repository: IStationsRepository
) : ViewModel(), EventHandler<ListEvent> {

    private val _viewState = MutableLiveData<ListViewState>(ListViewState.Loading)
    val viewState: LiveData<ListViewState> = _viewState

    override fun obtainEvent(event: ListEvent) {
        when (val currentViewState = _viewState.value) {
            is ListViewState.Display -> reduce(event, currentViewState)
            is ListViewState.Error -> reduce(event, currentViewState)
            is ListViewState.Loading -> reduce(event, currentViewState)
        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.Display) {
        when (event) {

        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.Error) {
        when (event) {

        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.Loading) {
        when (event) {
            ListEvent.EnterScreen -> fetchData()
        }
    }

    private fun fetchData(needReload: Boolean = false) {
        if (needReload) _viewState.postValue(ListViewState.Loading)

        viewModelScope.launch {
            val data = repository.fetchStations()

            if (data.isNotEmpty()) {
                _viewState.postValue(
                    ListViewState.Display(
                        data = data
                    )
                )
            } else {
                _viewState.postValue(
                    ListViewState.Error(msgRes = R.string.error_no_items_found)
                )
            }
        }
    }
}