package com.thejunglegiant.teslastations.domain.repository

import com.thejunglegiant.teslastations.domain.entity.StationEntity

interface IStationsRepository {
    suspend fun fetchStations(): List<StationEntity>
}