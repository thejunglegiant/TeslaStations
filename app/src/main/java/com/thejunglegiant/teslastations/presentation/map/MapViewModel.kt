package com.thejunglegiant.teslastations.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.domain.entity.DirectionItem
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.extensions.simResponseDelay
import com.thejunglegiant.teslastations.presentation.core.EventHandler
import com.thejunglegiant.teslastations.presentation.map.models.MapAction
import com.thejunglegiant.teslastations.presentation.map.models.MapEvent
import com.thejunglegiant.teslastations.presentation.map.models.MapViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: IStationsRepository
) : ViewModel(), EventHandler<MapEvent> {

    private val stationsList = mutableListOf<StationEntity>()

    private val _viewState = MutableStateFlow<MapViewState>(MapViewState.Loading)
    val viewState = _viewState.asStateFlow()

    private val _action = MutableSharedFlow<MapAction>()
    val action = _action.asSharedFlow()

    private var job: Job? = null

    override fun obtainEvent(event: MapEvent) {
        when (val currentState = _viewState.value) {
            is MapViewState.Direction -> reduce(event, currentState)
            is MapViewState.Display -> reduce(event, currentState)
            is MapViewState.Error -> reduce(event, currentState)
            is MapViewState.Loading -> reduce(event, currentState)
            is MapViewState.ItemDetails -> reduce(event, currentState)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.Display) {
        when (event) {
            MapEvent.EnterScreen -> fetchData()
            is MapEvent.ItemClicked -> getItem(event.item)
            is MapEvent.UndoItemDeleteClicked -> undoDeleteItem(event.item)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.ItemDetails) {
        when (event) {
            MapEvent.EnterScreen -> fetchData()
            MapEvent.ItemDirectionClicked -> _viewState.value = MapViewState.Loading
            MapEvent.ItemDetailsClosed -> _viewState.value = MapViewState.Display(stationsList)
            is MapEvent.ItemClicked -> getItem(event.item)
            is MapEvent.ItemDeleteClicked -> deleteItem(event.item)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.Direction) {
        when (event) {
            MapEvent.ItemDirectionCloseClicked -> fetchData(needReload = false)
            is MapEvent.ItemClicked -> getItem(event.item)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.Error) {
        when (event) {
            MapEvent.ReloadScreen -> fetchData(needReload = true)
            is MapEvent.ItemClicked -> getItem(event.item)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.Loading) {
        when (event) {
            MapEvent.EnterScreen -> fetchData()
            is MapEvent.ItemDirectionFound -> getRoute(event.from, event.to)
        }
    }

    private fun undoDeleteItem(item: StationEntity) {
        job?.cancel()
        _viewState.value = MapViewState.ItemDetails(item = item, addToMap = true)
    }

    private fun deleteItem(item: StationEntity) {
        _viewState.value = MapViewState.Loading

        job = Job().also { job ->
            viewModelScope.launch(Dispatchers.IO + job) {
                stationsList.remove(item)
                _viewState.value = MapViewState.Display(stationsList)
                _action.emit(MapAction.ItemDeleted(item))
                fetchData()

                delay(4000)
                repository.hideStation(item)
            }
        }
    }

    private fun getItem(station: StationEntity) {
        _viewState.value = MapViewState.Loading

        viewModelScope.launch {
            simResponseDelay()

            _viewState.value = MapViewState.ItemDetails(station)
        }
    }

    private fun fetchData(needReload: Boolean = false) {
        if (needReload) _viewState.value = MapViewState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.fetchStations()

            if (data.isNotEmpty()) {
                stationsList.clear()
                stationsList.addAll(data)
                _viewState.value = MapViewState.Display(data = data)
            } else {
                _viewState.value = MapViewState.Error(msgRes = R.string.error_no_items_found)
            }
        }
    }

    private fun getRoute(from: LatLng, to: LatLng) {
        _viewState.value = MapViewState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val route = repository.getDirection(from, to)

            if (route == null) {
                _viewState.value = MapViewState.Error(msgRes = R.string.error_no_direction)
            } else {
                _viewState.value = MapViewState.Direction(route)
            }
        }
    }
}