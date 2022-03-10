package com.thejunglegiant.teslastations.data.remote.model

import com.google.gson.annotations.SerializedName

data class CountryDTO(
    @SerializedName("iso") val iso: String,
    @SerializedName("country_name") val name: String,
    @SerializedName("lat_min") val minLat: Double,
    @SerializedName("lat_max") val maxLat: Double,
    @SerializedName("lon_min") val minLng: Double,
    @SerializedName("lon_max") val maxLng: Double,
)
