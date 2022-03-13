package com.thejunglegiant.teslastations.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.thejunglegiant.teslastations.presentation.cluster.MapClusterItem
import java.io.Serializable

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
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
    @ColumnInfo (name = "contact_number") val contactNumber: String,
    val status: Status = Status.VISIBLE,
) : Serializable, MapClusterItem() {

    enum class Status { VISIBLE, HIDDEN }

    @Ignore
    override val clusterItemId: Long = id

    @Ignore
    override val clusterItemLatitude: Double = latitude

    @Ignore
    override val clusterItemLongitude: Double = longitude

    @Ignore
    override val clusterItemSnippet: String = address
}
