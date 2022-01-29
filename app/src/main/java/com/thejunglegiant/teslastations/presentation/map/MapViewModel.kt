package com.thejunglegiant.teslastations.presentation.map

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thejunglegiant.teslastations.data.model.StationDTO
import com.thejunglegiant.teslastations.utils.getJsonDataFromAsset
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val _stationsList = MutableLiveData<List<StationDTO>>()
    val stationsList: LiveData<List<StationDTO>> = _stationsList

    init {
        val jsonFileString = getJsonDataFromAsset(context, "stations.json")
        val gson = Gson()
        val listPersonType = object : TypeToken<List<StationDTO>>() {}.type

        val stations: List<StationDTO> = gson.fromJson(jsonFileString, listPersonType)
        _stationsList.postValue(stations)
    }
}