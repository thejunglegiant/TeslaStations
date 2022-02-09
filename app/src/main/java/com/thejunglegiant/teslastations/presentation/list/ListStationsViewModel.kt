package com.thejunglegiant.teslastations.presentation.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.presentation.core.EventHandler
import com.thejunglegiant.teslastations.presentation.list.models.ListEvent
import com.thejunglegiant.teslastations.presentation.list.models.ListViewState
import kotlinx.coroutines.launch

class ListStationsViewModel(
    private val repository: IStationsRepository
) : ViewModel(), EventHandler<ListEvent> {

    private var page = FIRST_PAGE

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
            ListEvent.LoadMoreStations -> getStations()
        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.Error) {
        when (event) {

        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.Loading) {
        when (event) {
            ListEvent.EnterScreen -> getStations(isFirstPage = true)
        }
    }

    private fun getStations(isFirstPage: Boolean = false) {
        page++
        _viewState.postValue(ListViewState.Loading)

        viewModelScope.launch {
            val data = repository.getStations(
                limit = PAGE_LIMIT,
                offset = if (isFirstPage) 0 else page * PAGE_LIMIT
            )

            _viewState.postValue(
                ListViewState.Display(
                    data = data
                )
            )
        }
    }

    companion object {
        const val PAGE_LIMIT = 20
        private const val FIRST_PAGE = 0
    }
}