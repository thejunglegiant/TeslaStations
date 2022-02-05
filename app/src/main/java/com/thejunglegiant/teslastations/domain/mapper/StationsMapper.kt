package com.thejunglegiant.teslastations.domain.mapper

import android.text.Html
import com.thejunglegiant.teslastations.data.model.ContactDTO
import com.thejunglegiant.teslastations.data.model.StationDTO
import com.thejunglegiant.teslastations.domain.entity.ContactEntity
import com.thejunglegiant.teslastations.domain.entity.StationEntity

fun StationDTO.toStationEntity(): StationEntity = StationEntity(
    address = address,
    city = city,
    country = country,
    latitude = latitude.toDouble(),
    longitude = longitude.toDouble(),
    hours = hours,
    id = id,
    stationTitle = title,
    state = state?: "",
    region = region,
    description = Html.fromHtml(description).toString().trim(),
    contacts = contacts.map { it.toContactEntity() }
)

fun ContactDTO.toContactEntity(): ContactEntity = ContactEntity(
    label = label,
    number = number.trim()
)