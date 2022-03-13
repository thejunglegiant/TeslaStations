package com.thejunglegiant.teslastations.domain.mapper

import android.text.Html
import com.thejunglegiant.teslastations.data.remote.model.StationDTO
import com.thejunglegiant.teslastations.domain.entity.StationEntity

fun StationDTO.toStationEntity(): StationEntity = StationEntity(
    id = id.toLong(),
    title = title,
    address = address ?: "",
    city = city,
    country = country,
    latitude = latitude.toDouble(),
    longitude = longitude.toDouble(),
    hours = hours,
    stationTitle = title,
    state = state ?: "",
    region = region,
    description = Html.fromHtml(description).toString().trim(),
    contactNumber = contactNumber ?: ""
)