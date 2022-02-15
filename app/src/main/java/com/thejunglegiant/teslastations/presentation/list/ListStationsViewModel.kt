package com.thejunglegiant.teslastations.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLngBounds
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.presentation.core.EventHandler
import com.thejunglegiant.teslastations.presentation.list.models.ListEvent
import com.thejunglegiant.teslastations.presentation.list.models.ListViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListStationsViewModel(
    private val repository: IStationsRepository
) : ViewModel(), EventHandler<ListEvent> {

    private var page = FIRST_PAGE
    private var filterBounds: LatLngBounds? = null

    private val _viewState = MutableStateFlow<ListViewState>(ListViewState.Loading)
    val viewState = _viewState.asStateFlow()

    override fun obtainEvent(event: ListEvent) {
        when (val currentViewState = _viewState.value) {
            is ListViewState.Display -> reduce(event, currentViewState)
            is ListViewState.DisplayMore -> reduce(event, currentViewState)
            is ListViewState.Error -> reduce(event, currentViewState)
            is ListViewState.Loading -> reduce(event, currentViewState)
        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.Display) {
        when (event) {
            ListEvent.LoadMoreStations -> getStations()
            is ListEvent.FilterList -> getStations(isFirstPage = true, bounds = event.bounds)
        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.DisplayMore) {
        when (event) {
            ListEvent.LoadMoreStations -> getStations()
            is ListEvent.FilterList -> getStations(isFirstPage = true, bounds = event.bounds)
        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.Error) {
        when (event) {
            is ListEvent.FilterList -> getStations(isFirstPage = true, bounds = event.bounds)
        }
    }

    private fun reduce(event: ListEvent, currentViewState: ListViewState.Loading) {
        when (event) {
            ListEvent.EnterScreen -> getStations(isFirstPage = true)
        }
    }

    private fun getStations(isFirstPage: Boolean = false, bounds: LatLngBounds? = null) {
        _viewState.value = ListViewState.Loading

        val offset = if (isFirstPage) {
            page = FIRST_PAGE
            if (bounds != null) filterBounds = bounds
            0
        } else {
            ++page * PAGE_LIMIT
        }
        viewModelScope.launch {
            val data = repository.getStations(
                limit = PAGE_LIMIT,
                offset = offset,
                bounds = filterBounds
            )

            _viewState.value =
                if (page == FIRST_PAGE) {
                    ListViewState.Display(
                        data = data
                    )
                } else {
                    ListViewState.DisplayMore(
                        data = data
                    )
                }
        }
    }

    companion object {
        const val PAGE_LIMIT = 20
        private const val FIRST_PAGE = 0
    }
}