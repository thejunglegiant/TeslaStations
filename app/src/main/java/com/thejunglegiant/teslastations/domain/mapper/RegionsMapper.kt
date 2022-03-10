package com.thejunglegiant.teslastations.domain.mapper

import com.google.android.gms.maps.model.LatLngBounds
import com.thejunglegiant.teslastations.data.remote.model.ContinentDTO
import com.thejunglegiant.teslastations.data.remote.model.CountryDTO
import com.thejunglegiant.teslastations.domain.entity.BoundsItem
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.CountryEntity

fun CountryDTO.toCountryEntity(continentId: Int): CountryEntity = CountryEntity(
    iso = iso,
    continentId = continentId,
    name = name,
    minLat = minLat,
    maxLat = maxLat,
    minLng = minLng,
    maxLng = maxLng
)

fun ContinentDTO.toContinentEntity(): ContinentEntity = ContinentEntity(
    id = id,
    name = name,
    minLat = minLat,
    maxLat = maxLat,
    minLng = minLng,
    maxLng = maxLng
)

fun BoundsItem.toLatLngBounds(): LatLngBounds = LatLngBounds(southwest, northeast)