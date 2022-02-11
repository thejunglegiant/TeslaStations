package com.thejunglegiant.teslastations.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

@Entity(tableName = "continents")
data class ContinentEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "continent_name") val name: String,
    @ColumnInfo(name = "lat_min") val minLat: Double,
    @ColumnInfo(name = "lat_max") val maxLat: Double,
    @ColumnInfo(name = "lon_min") val minLng: Double,
    @ColumnInfo(name = "lon_max") val maxLng: Double,
) : Serializable {
    fun getBounds(): BoundsItem =
        BoundsItem(
            LatLng(minLat, minLng),
            LatLng(maxLat, maxLng)
        )
}
