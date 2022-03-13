package com.thejunglegiant.teslastations.data.repository

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thejunglegiant.teslastations.data.database.dao.RegionsDao
import com.thejunglegiant.teslastations.data.database.dao.StationsDao
import com.thejunglegiant.teslastations.data.remote.Api
import com.thejunglegiant.teslastations.data.remote.model.ContinentDTO
import com.thejunglegiant.teslastations.data.remote.model.CountryDTO
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.mapper.toContinentEntity
import com.thejunglegiant.teslastations.domain.mapper.toCountryEntity
import com.thejunglegiant.teslastations.domain.mapper.toLatLngBounds
import com.thejunglegiant.teslastations.domain.mapper.toStationEntity
import com.thejunglegiant.teslastations.domain.repository.IPopulateRepository
import com.thejunglegiant.teslastations.extensions.getJsonDataFromAsset
import com.thejunglegiant.teslastations.extensions.logd
import com.thejunglegiant.teslastations.extensions.loge
import com.thejunglegiant.teslastations.utils.DB_NAME
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.system.measureTimeMillis

class PopulateRepository(
    private val context: Context,
    private val gson: Gson,
    private val stationsDao: StationsDao,
    private val regionsDao: RegionsDao,
    private val api: Api,
) : IPopulateRepository {

    private suspend fun prepopulatedTeslaData(): List<StationEntity> {
        val response = api.getLocations("destination_charger")
        return if (response.isSuccessful) {
            val stations = response.body()!!.map { it.toStationEntity() }
            logd("${stations.size} countries were found!")
            stations
        } else {
            loge(response.message())
            listOf()
        }
    }

    private fun prepopulatedCountriesData(): List<CountryDTO> {
        val jsonFileString = getJsonDataFromAsset(context, COUNTRIES_FILE)
        val type = object : TypeToken<List<CountryDTO>>() {}.type

        val countries: List<CountryDTO> = gson.fromJson(jsonFileString, type)
        logd("${countries.size} countries were found!")
        return countries
    }

    private fun prepopulatedContinentsData(): List<ContinentEntity> {
        val jsonFileString = getJsonDataFromAsset(context, CONTINENTS_FILE)
        val type = object : TypeToken<List<ContinentDTO>>() {}.type

        val continents: List<ContinentDTO> = gson.fromJson(jsonFileString, type)
        logd("${continents.size} stations were found!")
        return continents.map { it.toContinentEntity() }
    }

    override suspend fun initDb(): Boolean {
        return try {
            val db = context.getDatabasePath(DB_NAME)
            if (!db.exists() || stationsDao.getAllCount() < 1) {
                coroutineScope {
                    val time = measureTimeMillis {
                        awaitAll(
                            async {
                                val continents = prepopulatedContinentsData()
                                regionsDao.insertAllContinents(continents)

                                val countries = prepopulatedCountriesData()
                                    .map { item ->
                                        var continentId = 0
                                        for (continent in continents) {
                                            if (continent.getBounds().toLatLngBounds()
                                                    .contains(LatLng(item.minLat, item.minLng))
                                            ) {
                                                continentId = continent.id
                                                break
                                            }
                                        }
                                        item.toCountryEntity(continentId)
                                    }
                                regionsDao.insertAllCountries(countries)
                            },
                            async {
                                val data = prepopulatedTeslaData()
                                stationsDao.insertAll(data)
                            }
                        )
                    }

                    logd("Prepopulated data were written into database in ${time / 1000} sec")
                }
            }

            true
        } catch (e: Exception) {
            loge(e.message ?: "something wrong")
            false
        }
    }

    companion object {
        private const val COUNTRIES_FILE = "countries.json"
        private const val CONTINENTS_FILE = "continents.json"
    }
}