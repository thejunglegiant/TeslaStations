package com.thejunglegiant.teslastations.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.thejunglegiant.teslastations.domain.entity.StationEntity

interface IStationsRepository {
    suspend fun initDb(): Boolean

    suspend fun fetchStations(): List<StationEntity>

    suspend fun getDirection(from: LatLng, to: LatLng): Pair<LatLngBounds, String>?
}