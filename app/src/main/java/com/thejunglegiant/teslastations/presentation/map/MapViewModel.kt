package com.thejunglegiant.teslastations.presentation.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapViewModel(
    private val repository: IStationsRepository
) : ViewModel() {

    private val _stationsList = MutableLiveData<List<StationEntity>>()
    val stationsList: LiveData<List<StationEntity>> = _stationsList

    private val _route = MutableLiveData<String>()
    val route: LiveData<String> = _route

    init {
        viewModelScope.launch {
            _stationsList.value = repository.fetchStations()
        }
    }

    fun getRoute(from: LatLng, to: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            _route.postValue(repository.getDirection(from, to))
        }
    }
}