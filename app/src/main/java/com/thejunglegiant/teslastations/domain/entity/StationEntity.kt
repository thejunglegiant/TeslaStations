package com.thejunglegiant.teslastations.domain.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.io.Serializable

@Entity(tableName = "station")
data class StationEntity(
    @PrimaryKey
    val id: String,
    val address: String,
    val city: String,
    val country: String,
    val hours: String,
    val latitude: Double,
    val longitude: Double,
    val stationTitle: String,
    val state: String,
    val region: String,
    val description: String,
    val status: Status = Status.VISIBLE,
    @Embedded val contact: ContactEntity?
) : Serializable, ClusterItem {

    enum class Status { VISIBLE, HIDDEN }

    @Ignore
    override fun getPosition(): LatLng = LatLng(latitude, longitude)

    @Ignore
    override fun getTitle(): String = stationTitle

    @Ignore
    override fun getSnippet(): String = address
}
