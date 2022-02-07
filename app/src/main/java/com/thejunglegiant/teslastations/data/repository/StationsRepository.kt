package com.thejunglegiant.teslastations.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.thejunglegiant.teslastations.BuildConfig
import com.thejunglegiant.teslastations.data.model.StationDTO
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.mapper.toStationEntity
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.extensions.getJsonDataFromAsset
import com.thejunglegiant.teslastations.extensions.loge
import okhttp3.OkHttpClient
import okhttp3.Request

class StationsRepository(
    private val context: Context,
    private val httpClient: OkHttpClient,
    private val gson: Gson,
) : IStationsRepository {

    override suspend fun fetchStations(): List<StationEntity> {
        val jsonFileString = getJsonDataFromAsset(context, "stations_no_limit.json")
        val gson = Gson()
        val listPersonType = object : TypeToken<List<StationDTO>>() {}.type

        val stations: List<StationDTO> = gson.fromJson(jsonFileString, listPersonType)
        Log.d("StationsRepository", "${stations.size} stations were found!")
        return stations.map { it.toStationEntity() }
    }

    override suspend fun getDirection(from: String, to: String): String? {
        val request = Request.Builder()
            .url("${DIRECTIONS_BASE_URL}origin=$from&destination=$to&key=${BuildConfig.MAPS_API_KEY}")
            .method("GET", null)
            .build()
        val response = httpClient.newCall(request).execute()
        val obj = gson.fromJson(response.body!!.string(), JsonObject::class.java)
        return obj
            .get(ROUTES_ARRAY).asJsonArray
            .firstOrNull()?.asJsonObject
            ?.get(POLYLINE_OBJECT)?.asJsonObject
            ?.get(POLYLINE_STRING)?.asString
    }

    override suspend fun getDirection(from: LatLng, to: LatLng): String? {
        val request = Request.Builder()
            .url("${DIRECTIONS_BASE_URL}origin=${from.latitude},${from.longitude}&destination=${to.latitude},${to.longitude}&key=${BuildConfig.MAPS_API_KEY}")
            .method("GET", null)
            .build()
        val response = httpClient.newCall(request).execute()
        val obj = gson.fromJson(response.body!!.string(), JsonObject::class.java)
        return obj
            .get(ROUTES_ARRAY).asJsonArray
            .firstOrNull()?.asJsonObject
            ?.get(POLYLINE_OBJECT)?.asJsonObject
            ?.get(POLYLINE_STRING)?.asString
    }

    companion object {
        private const val DIRECTIONS_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?"
        private const val ROUTES_ARRAY = "routes"
        private const val POLYLINE_OBJECT = "overview_polyline"
        private const val POLYLINE_STRING = "points"
    }
}