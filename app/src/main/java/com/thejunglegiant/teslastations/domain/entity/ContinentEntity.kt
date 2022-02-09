package com.thejunglegiant.teslastations.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

@Entity(tableName = "continents")
data class ContinentEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "country_name") val name: String,
    @ColumnInfo(name = "lat_min") val minLat: Double,
    @ColumnInfo(name = "lat_max") val maxLat: Double,
    @ColumnInfo(name = "lon_min") val minLng: Double,
    @ColumnInfo(name = "lon_max") val maxLng: Double,
) {
    fun getBounds(): LatLngBounds =
        LatLngBounds(
            LatLng(minLat, minLng),
            LatLng(maxLat, maxLng)
        )
}
