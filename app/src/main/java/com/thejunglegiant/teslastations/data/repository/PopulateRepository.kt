package com.thejunglegiant.teslastations.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thejunglegiant.teslastations.data.database.dao.RegionsDao
import com.thejunglegiant.teslastations.data.database.dao.StationsDao
import com.thejunglegiant.teslastations.data.model.ContinentDTO
import com.thejunglegiant.teslastations.data.model.CountryDTO
import com.thejunglegiant.teslastations.data.model.StationDTO
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.mapper.toContinentEntity
import com.thejunglegiant.teslastations.domain.mapper.toCountryEntity
import com.thejunglegiant.teslastations.domain.mapper.toStationEntity
import com.thejunglegiant.teslastations.domain.repository.IPopulateRepository
import com.thejunglegiant.teslastations.extensions.getJsonDataFromAsset
import com.thejunglegiant.teslastations.extensions.loge
import com.thejunglegiant.teslastations.utils.DB_NAME

class PopulateRepository(
    private val context: Context,
    private val gson: Gson,
    private val stationsDao: StationsDao,
    private val regionsDao: RegionsDao,
) : IPopulateRepository {

    private fun prepopulatedStationsData(): List<StationEntity> {
        val jsonFileString = getJsonDataFromAsset(context, STATIONS_FILE)
        val gson = Gson()
        val type = object : TypeToken<List<StationDTO>>() {}.type

        val stations: List<StationDTO> = gson.fromJson(jsonFileString, type)
        Log.d(TAG, "${stations.size} stations were found!")
        return stations.map { it.toStationEntity() }
    }

    private fun prepopulatedCountriesData(): List<CountryDTO> {
        val jsonFileString = getJsonDataFromAsset(context, COUNTRIES_FILE)
        val gson = Gson()
        val type = object : TypeToken<List<CountryDTO>>() {}.type

        val countries: List<CountryDTO> = gson.fromJson(jsonFileString, type)
        Log.d(TAG, "${countries.size} countries were found!")
        return countries
    }

    private fun prepopulatedContinentsData(): List<ContinentEntity> {
        val jsonFileString = getJsonDataFromAsset(context, CONTINENTS_FILE)
        val gson = Gson()
        val type = object : TypeToken<List<ContinentDTO>>() {}.type

        val continents: List<ContinentDTO> = gson.fromJson(jsonFileString, type)
        Log.d(TAG, "${continents.size} stations were found!")
        return continents.map { it.toContinentEntity() }
    }

    override suspend fun initDb(): Boolean {
        return try {
            val db = context.getDatabasePath(DB_NAME)
            if (!db.exists()) {
                val continents = prepopulatedContinentsData()
                regionsDao.insertAllContinents(continents)

                val countries = prepopulatedCountriesData()
                    .map { item ->
                        var continentId = 0
                        for (continent in continents) {
                            if (continent.getBounds().contains(LatLng(item.minLat, item.minLng))) {
                                continentId = continent.id
                                break
                            }
                        }
                        item.toCountryEntity(continentId)
                    }
                regionsDao.insertAllCountries(countries)

                val stations = prepopulatedStationsData()
                stationsDao.insertAll(stations)
                loge(
                    StationsRepository.TAG,
                    "Prepopulated data were written into database!"
                )
            }

            true
        } catch (e: Exception) {
            loge(StationsRepository.TAG, e.message ?: "something wrong")
            false
        }
    }

    companion object {
        val TAG: String = PopulateRepository::class.java.simpleName

        private const val STATIONS_FILE = "stations.json"
        private const val COUNTRIES_FILE = "countries.json"
        private const val CONTINENTS_FILE = "continents.json"
    }
}