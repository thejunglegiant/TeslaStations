package com.thejunglegiant.teslastations.domain.mapper

import android.text.Html
import com.thejunglegiant.teslastations.data.model.ContactDTO
import com.thejunglegiant.teslastations.data.model.StationDTO
import com.thejunglegiant.teslastations.domain.entity.ContactEntity
import com.thejunglegiant.teslastations.domain.entity.StationEntity

fun StationDTO.toStationEntity(): StationEntity = StationEntity(
    id = id.toLong(),
    address = address,
    city = city,
    country = country,
    latitude = latitude.toDouble(),
    longitude = longitude.toDouble(),
    hours = hours,
    stationTitle = title,
    state = state?: "",
    region = region,
    description = Html.fromHtml(description).toString().trim(),
    contact = contacts.firstOrNull()?.toContactEntity()
)

fun ContactDTO.toContactEntity(): ContactEntity = ContactEntity(
    label = label,
    number = number.trim()
)