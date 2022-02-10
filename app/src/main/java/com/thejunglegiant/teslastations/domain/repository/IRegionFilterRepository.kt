package com.thejunglegiant.teslastations.domain.repository

import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.CountryEntity

interface IRegionFilterRepository {

    suspend fun fetchContinents(): List<ContinentEntity>

    suspend fun fetchCountries(): List<CountryEntity>

    suspend fun fetchCountriesByContinent(continentId: Int): List<CountryEntity>
}