package com.thejunglegiant.teslastations.data.remote

import com.thejunglegiant.teslastations.data.remote.model.StationDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("all-locations")
    suspend fun getLocations (@Query("type") type: String): Response<List<StationDTO>>
}