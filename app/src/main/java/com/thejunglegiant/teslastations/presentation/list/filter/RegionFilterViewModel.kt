package com.thejunglegiant.teslastations.presentation.list.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.repository.IRegionFilterRepository
import com.thejunglegiant.teslastations.presentation.core.EventHandler
import com.thejunglegiant.teslastations.presentation.list.filter.models.FilterEvent
import com.thejunglegiant.teslastations.presentation.list.filter.models.FilterViewState
import kotlinx.coroutines.launch

class RegionFilterViewModel(
    private val repository: IRegionFilterRepository
) : ViewModel(), EventHandler<FilterEvent> {

    private val _viewState = MutableLiveData<FilterViewState>(FilterViewState.Loading)
    val viewState: LiveData<FilterViewState> = _viewState

    override fun obtainEvent(event: FilterEvent) {
        when (val currentViewState = _viewState.value) {
            is FilterViewState.Display -> reduce(event, currentViewState)
            is FilterViewState.Error -> reduce(event, currentViewState)
            is FilterViewState.Loading -> reduce(event, currentViewState)
        }
    }

    private fun reduce(event: FilterEvent, viewState: FilterViewState.Display) {
        when (event) {
            is FilterEvent.ContinentClicked -> fetchCountries(
                event.continentId,
                viewState.continents
            )
            is FilterEvent.ContinentDisabled -> fetchCountries(continentsData = viewState.continents)
        }
    }

    private fun reduce(event: FilterEvent, viewState: FilterViewState.Error) {
        when (event) {

        }
    }

    private fun reduce(event: FilterEvent, viewState: FilterViewState.Loading) {
        when (event) {
            FilterEvent.EnterScreen -> fetchData()
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            val continents = repository.fetchContinents()
            val countries = repository.fetchCountries()

            _viewState.postValue(
                FilterViewState.Display(
                    continents = continents,
                    countries = countries
                )
            )
        }
    }

    private fun fetchCountries(continentId: Int? = null, continentsData: List<ContinentEntity>) {
        viewModelScope.launch {
            val data = continentId?.let { repository.fetchCountriesByContinent(continentId) }
                ?: repository.fetchCountries()

            _viewState.postValue(
                FilterViewState.Display(
                    continents = continentsData,
                    countries = data
                )
            )
        }
    }
}