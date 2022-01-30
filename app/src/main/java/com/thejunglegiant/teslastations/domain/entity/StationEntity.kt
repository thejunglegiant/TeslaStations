package com.thejunglegiant.teslastations.domain.entity

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class StationEntity(
    val address: String,
    val city: String,
    val country: String,
    val hours: String,
    val latitude: Double,
    val longitude: Double,
    val id: String,
    val stationTitle: String,
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(latitude, longitude)

    override fun getTitle(): String = stationTitle

    override fun getSnippet(): String = address
}
