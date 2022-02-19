package com.thejunglegiant.teslastations.presentation.map.models

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.thejunglegiant.teslastations.domain.entity.StationEntity

sealed class MapViewState {
    object Loading : MapViewState()
    data class ItemDetails(val item: StationEntity, val addToMap: Boolean = false) : MapViewState()
    data class Error(val msg: String? = null, @StringRes val msgRes: Int? = null) : MapViewState()
    data class Display(val data: List<StationEntity>, val deletedItem: StationEntity? = null) : MapViewState()
    data class Direction(val bounds: LatLngBounds, val points: List<LatLng>) : MapViewState()
}
