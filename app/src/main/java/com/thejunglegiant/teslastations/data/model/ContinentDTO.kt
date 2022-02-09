package com.thejunglegiant.teslastations.data.model

import com.google.gson.annotations.SerializedName

data class ContinentDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("continent_name") val name: String,
    @SerializedName("lat_min") val minLat: Double,
    @SerializedName("lat_max") val maxLat: Double,
    @SerializedName("lon_min") val minLng: Double,
    @SerializedName("lon_max") val maxLng: Double,
)
