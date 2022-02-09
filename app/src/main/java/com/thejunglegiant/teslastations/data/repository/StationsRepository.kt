package com.thejunglegiant.teslastations.data.repository

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.thejunglegiant.teslastations.BuildConfig
import com.thejunglegiant.teslastations.data.database.dao.StationsDao
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.extensions.simResponseDelay
import okhttp3.OkHttpClient
import okhttp3.Request

class StationsRepository(
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    private val stationsDao: StationsDao,
) : IStationsRepository {

    override suspend fun fetchStations(): List<StationEntity> {
        return stationsDao.getAll()
    }

    override suspend fun getStations(limit: Int, offset: Int): List<StationEntity> {
        simResponseDelay()
        return stationsDao.getStationsOffset(limit = limit, offset = offset)
    }

    override suspend fun getStationsByBounds(
        limit: Int,
        offset: Int,
        bounds: LatLngBounds
    ): List<StationEntity> {
        simResponseDelay()
        return stationsDao.getStationsOffsetByBounds(
            limit = limit,
            offset = offset,
            fromLat = bounds.southwest.latitude,
            toLat = bounds.northeast.latitude,
            fromLng = bounds.southwest.longitude,
            toLng = bounds.northeast.longitude
        )
    }

    override suspend fun hideStation(station: StationEntity): StationEntity {
        val hiddenStation = station.copy(status = StationEntity.Status.HIDDEN)
        stationsDao.updateStation(hiddenStation)
        return hiddenStation
    }

    override suspend fun showStation(station: StationEntity): StationEntity {
        val visibleStation = station.copy(status = StationEntity.Status.VISIBLE)
        stationsDao.updateStation(visibleStation)
        return visibleStation
    }

    override suspend fun getDirection(from: LatLng, to: LatLng): Pair<LatLngBounds, String>? {
        val request = Request.Builder()
            .url(
                "${DIRECTIONS_BASE_URL}origin=${from.latitude},${from.longitude}" +
                        "&destination=${to.latitude},${to.longitude}&key=${BuildConfig.MAPS_API_KEY}"
            )
            .method("GET", null)
            .build()

        val response = httpClient.newCall(request).execute()
        val route = gson.fromJson(response.body!!.string(), JsonObject::class.java)
            .get(ROUTES_ARRAY).asJsonArray
            .firstOrNull()?.asJsonObject

        return if (route == null) {
            null
        } else {
            val northeast = route.get(ROUTE_BOUNDS).asJsonObject.get(BOUNDS_NORTHEAST).asJsonObject
            val southwest = route.get(ROUTE_BOUNDS).asJsonObject.get(BOUNDS_SOUTHWEST).asJsonObject
            val bounds = LatLngBounds.builder()
                .include(
                    LatLng(
                        northeast.get(LATITUDE).asDouble,
                        northeast.get(LONGITUDE).asDouble
                    )
                )
                .include(
                    LatLng(
                        southwest.get(LATITUDE).asDouble,
                        southwest.get(LONGITUDE).asDouble
                    )
                )
                .build()

            val polylineString = route
                .get(POLYLINE_OBJECT).asJsonObject
                .get(POLYLINE_STRING).asString

            Pair(bounds, polylineString)
        }

    }

    companion object {
        val TAG: String = StationsRepository::class.java.simpleName
        private const val DIRECTIONS_BASE_URL =
            "https://maps.googleapis.com/maps/api/directions/json?"
        private const val ROUTES_ARRAY = "routes"
        private const val ROUTE_BOUNDS = "bounds"
        private const val BOUNDS_NORTHEAST = "northeast"
        private const val BOUNDS_SOUTHWEST = "southwest"
        private const val LATITUDE = "lat"
        private const val LONGITUDE = "lng"
        private const val POLYLINE_OBJECT = "overview_polyline"
        private const val POLYLINE_STRING = "points"
    }
}