package com.thejunglegiant.teslastations.data.remote.model

import com.google.gson.annotations.SerializedName

data class StationDTO(
    @SerializedName("address") val address: String?,
    @SerializedName("city") val city: String,
    @SerializedName("country") val country: String,
    @SerializedName("hours") val hours: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("nid") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("region") val region: String,
    @SerializedName("province_state") val state: String?,
    @SerializedName("contact_number") val contactNumber: String?,
    @SerializedName("chargers") val description: String,
)
