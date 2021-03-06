package com.thejunglegiant.teslastations.presentation.map.models

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.thejunglegiant.teslastations.domain.entity.DirectionItem
import com.thejunglegiant.teslastations.domain.entity.StationEntity

sealed class MapViewState {
    object Loading : MapViewState()
    object Idle : MapViewState()
    data class ItemDetails(val item: StationEntity) : MapViewState()
    data class Error(val msg: String? = null, @StringRes val msgRes: Int? = null) : MapViewState()
    data class Display(val data: List<StationEntity>) : MapViewState()
    data class Direction(val direction: DirectionItem) : MapViewState()
}
