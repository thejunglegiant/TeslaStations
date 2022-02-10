package com.thejunglegiant.teslastations.presentation.list.filter.models

sealed class FilterEvent {
    object EnterScreen : FilterEvent()
    object ContinentDisabled : FilterEvent()
    data class ContinentClicked(val continentId: Int) : FilterEvent()
    data class CountryClicked(val countryIso: String) : FilterEvent()
}
