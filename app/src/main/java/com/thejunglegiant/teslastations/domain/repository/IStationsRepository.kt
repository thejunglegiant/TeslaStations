package com.thejunglegiant.teslastations.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.thejunglegiant.teslastations.domain.entity.StationEntity

interface IStationsRepository {
    suspend fun fetchStations(): List<StationEntity>

    suspend fun getStations(limit: Int, offset: Int, bounds: LatLngBounds?): List<StationEntity>

    suspend fun hideStation(station: StationEntity): StationEntity

    suspend fun showStation(station: StationEntity): StationEntity

    suspend fun getDirection(from: LatLng, to: LatLng): Pair<LatLngBounds, String>?
}