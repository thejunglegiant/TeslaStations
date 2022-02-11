package com.thejunglegiant.teslastations.presentation.list.models

import com.google.android.gms.maps.model.LatLngBounds

sealed class ListEvent {
    object EnterScreen : ListEvent()
    object LoadMoreStations : ListEvent()
    data class FilterList(val bounds: LatLngBounds) : ListEvent()
}
