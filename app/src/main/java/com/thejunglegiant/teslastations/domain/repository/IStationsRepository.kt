package com.thejunglegiant.teslastations.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.thejunglegiant.teslastations.domain.entity.StationEntity

interface IStationsRepository {
    suspend fun fetchStations(): List<StationEntity>

    suspend fun getDirection(from: String, to: String): String

    suspend fun getDirection(from: LatLng, to: LatLng): String
}