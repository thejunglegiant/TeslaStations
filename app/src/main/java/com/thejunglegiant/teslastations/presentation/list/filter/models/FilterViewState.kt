package com.thejunglegiant.teslastations.presentation.list.filter.models

import androidx.annotation.StringRes
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.CountryEntity

sealed class FilterViewState {
    object Loading : FilterViewState()
    data class Error(val msg: String? = null, @StringRes val msgRes: Int? = null) : FilterViewState()
    data class Display(
        val continents: List<ContinentEntity>,
        val countries: List<CountryEntity>
    ) : FilterViewState()
}
