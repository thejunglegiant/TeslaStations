package com.thejunglegiant.teslastations.presentation.map.models

import com.thejunglegiant.teslastations.domain.entity.StationEntity

sealed class MapAction {
    data class ItemDeleted(val item: StationEntity) : MapAction()
}
