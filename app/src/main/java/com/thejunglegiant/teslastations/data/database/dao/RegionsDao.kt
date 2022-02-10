package com.thejunglegiant.teslastations.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.CountryEntity

@Dao
interface RegionsDao {

    @Query("SELECT * FROM continents ORDER BY continent_name")
    suspend fun getAllContinents(): List<ContinentEntity>

    @Query("SELECT * FROM countries ORDER BY country_name")
    suspend fun getAllCountries(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE continent_id = :continentId ORDER BY country_name")
    suspend fun getCountriesByContinent(continentId: Int): List<CountryEntity>

    @Insert
    suspend fun insertAllContinents(continents: List<ContinentEntity>)

    @Insert
    suspend fun insertAllCountries(countries: List<CountryEntity>)
}