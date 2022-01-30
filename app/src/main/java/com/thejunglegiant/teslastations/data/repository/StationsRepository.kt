package com.thejunglegiant.teslastations.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thejunglegiant.teslastations.data.model.StationDTO
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.mapper.toStationEntity
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.utils.getJsonDataFromAsset

class StationsRepository(
    private val context: Context
) : IStationsRepository {
    override suspend fun fetchStations(): List<StationEntity> {
        val jsonFileString = getJsonDataFromAsset(context, "stations_no_limit.json")
        val gson = Gson()
        val listPersonType = object : TypeToken<List<StationDTO>>() {}.type

        val stations: List<StationDTO> = gson.fromJson(jsonFileString, listPersonType)
        Log.d("StationsRepository", "${stations.size} were found!")
        return stations.map { it.toStationEntity() }
    }
}