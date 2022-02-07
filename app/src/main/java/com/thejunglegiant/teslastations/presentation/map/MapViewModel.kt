package com.thejunglegiant.teslastations.presentation.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.domain.entity.MapSettingsItem
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.presentation.core.EventHandler
import com.thejunglegiant.teslastations.presentation.map.models.MapEvent
import com.thejunglegiant.teslastations.presentation.map.models.MapViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class MapViewModel(
    private val repository: IStationsRepository
) : ViewModel(), EventHandler<MapEvent> {

    private val _mapViewState = MutableLiveData<MapViewState>(MapViewState.Loading)
    val mapViewState: LiveData<MapViewState> = _mapViewState

    override fun obtainEvent(event: MapEvent) {
        when (val currentState = _mapViewState.value) {
            is MapViewState.Direction -> reduce(event, currentState)
            is MapViewState.Display -> reduce(event, currentState)
            is MapViewState.Error -> reduce(event, currentState)
            is MapViewState.Loading -> reduce(event, currentState)
            is MapViewState.NoItems -> reduce(event, currentState)
            is MapViewState.ItemDetails -> reduce(event, currentState)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.Display) {
        when (event) {
            MapEvent.EnterScreen -> fetchData()
            MapEvent.MapModeClicked -> changeMapMode()
            is MapEvent.ItemClicked -> getItem(event.item)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.ItemDetails) {
        when (event) {
            MapEvent.EnterScreen -> fetchData()
            MapEvent.MapModeClicked -> changeMapMode()
            is MapEvent.ItemClicked -> getItem(event.item)
            is MapEvent.ItemDirectionClicked -> getRoute(event.from, event.to)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.Direction) {
        when (event) {
            MapEvent.MapModeClicked -> changeMapMode()
            MapEvent.EnterScreen -> fetchData(needReload = true)
            is MapEvent.ItemClicked -> getItem(event.item)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.Error) {
        when (event) {
            MapEvent.MapModeClicked -> changeMapMode()
            MapEvent.ReloadScreen -> fetchData(needReload = true)
            is MapEvent.ItemClicked -> getItem(event.item)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.Loading) {
        when (event) {
            MapEvent.EnterScreen -> fetchData(needReload = true)
        }
    }

    private fun reduce(event: MapEvent, currentViewState: MapViewState.NoItems) {
        when (event) {
            MapEvent.MapModeClicked -> changeMapMode()
            MapEvent.ReloadScreen -> fetchData(needReload = true)
        }
    }

    private fun getItem(station: StationEntity) {
        _mapViewState.postValue(MapViewState.Loading)

        viewModelScope.launch {
            // request delay simulation
            delay(Random.nextLong(100, 1000))

            _mapViewState.postValue(
                MapViewState.ItemDetails(station)
            )
        }
    }

    private fun changeMapMode() {
        _mapViewState.postValue(
            MapViewState.Display(
                data = emptyList(), settings = MapSettingsItem(false)
            )
        )
    }

    private fun fetchData(needReload: Boolean = false) {
        if (needReload) _mapViewState.postValue(MapViewState.Loading)

        viewModelScope.launch {
            val data = repository.fetchStations()

            if (data.isNotEmpty()) {
                _mapViewState.postValue(
                    MapViewState.Display(
                        data = data,
                        // TODO add DataStore
                        settings = MapSettingsItem()
                    )
                )
            } else {
                _mapViewState.postValue(MapViewState.NoItems)
            }
        }
    }

    private fun getRoute(from: LatLng, to: LatLng) {
        _mapViewState.postValue(MapViewState.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            val route = repository.getDirection(from, to)

            if (route == null) {
                _mapViewState.postValue(MapViewState.Error(msgRes = R.string.error_no_direction))
            } else {
                _mapViewState.postValue(MapViewState.Direction(PolyUtil.decode(route)))
            }
        }
    }
}