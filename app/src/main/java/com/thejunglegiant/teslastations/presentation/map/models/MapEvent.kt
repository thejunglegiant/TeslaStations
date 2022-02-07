package com.thejunglegiant.teslastations.presentation.map.models

import com.google.android.gms.maps.model.LatLng
import com.thejunglegiant.teslastations.domain.entity.StationEntity

sealed class MapEvent {
    object EnterScreen : MapEvent()
    object ReloadScreen : MapEvent()
    object MapModeClicked : MapEvent()
    data class ItemClicked(val item: StationEntity) : MapEvent()
    data class ItemDirectionClicked(val from: LatLng, val to: LatLng): MapEvent()
}
