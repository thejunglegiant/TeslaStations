package com.thejunglegiant.teslastations.presentation.map.models

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import com.thejunglegiant.teslastations.domain.entity.MapSettingsItem
import com.thejunglegiant.teslastations.domain.entity.StationEntity

sealed class MapViewState {
    object Loading : MapViewState()
    object NoItems : MapViewState()
    data class ItemDetails(val item: StationEntity) : MapViewState()
    data class Error(val msg: String? = null, @StringRes val msgRes: Int? = null) : MapViewState()
    data class Display(val data: List<StationEntity>, val settings: MapSettingsItem) : MapViewState()
    data class Direction(val points: List<LatLng>) : MapViewState()
}
