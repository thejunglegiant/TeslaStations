package com.thejunglegiant.teslastations.domain.mapper

import com.thejunglegiant.teslastations.data.model.StationDTO
import com.thejunglegiant.teslastations.domain.entity.StationEntity

fun StationDTO.toStationEntity(): StationEntity = StationEntity(
    address = address,
    city = city,
    country = country,
    latitude = latitude.toDouble(),
    longitude = longitude.toDouble(),
    hours = hours,
    id = nid,
    stationTitle = title
)