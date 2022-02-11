package com.thejunglegiant.teslastations.presentation.list.filter.models

import com.thejunglegiant.teslastations.domain.entity.CountryEntity

sealed class FilterEvent {
    object EnterScreen : FilterEvent()
    object ContinentDisabled : FilterEvent()
    data class ContinentClicked(val continentId: Int) : FilterEvent()
    data class CountryClicked(val country: CountryEntity) : FilterEvent()
}
