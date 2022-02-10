package com.thejunglegiant.teslastations.data.repository

import com.thejunglegiant.teslastations.data.database.dao.RegionsDao
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.CountryEntity
import com.thejunglegiant.teslastations.domain.repository.IRegionFilterRepository

class RegionFilterRepository(
    private val regionsDao: RegionsDao,
): IRegionFilterRepository {

    override suspend fun fetchContinents(): List<ContinentEntity> =
        regionsDao.getAllContinents()

    override suspend fun fetchCountries(): List<CountryEntity> =
        regionsDao.getAllCountries()

    override suspend fun fetchCountriesByContinent(continentId: Int): List<CountryEntity> =
        regionsDao.getCountriesByContinent(continentId)
}