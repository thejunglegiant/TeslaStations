package com.thejunglegiant.teslastations.data.database.converters

import androidx.room.TypeConverter
import com.thejunglegiant.teslastations.domain.entity.StationEntity

class StatusConverter {

    @TypeConverter
    fun convertFrom(status: Int?): StationEntity.Status? =
        status?.let { index -> StationEntity.Status.values()[index] }

    @TypeConverter
    fun convertTo(status: StationEntity.Status?): Int? =
        status?.let { item -> StationEntity.Status.values().indexOf(item) }
}