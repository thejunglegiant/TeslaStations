package com.thejunglegiant.teslastations.presentation.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import kotlinx.coroutines.launch

class MapViewModel(
    stationsRepository: IStationsRepository
) : ViewModel() {

    private val _stationsList = MutableLiveData<List<StationEntity>>()
    val stationsList: LiveData<List<StationEntity>> = _stationsList

    init {
        viewModelScope.launch {
            _stationsList.value = stationsRepository.fetchStations()
        }
    }
}