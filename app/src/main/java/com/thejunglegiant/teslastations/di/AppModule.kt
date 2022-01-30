package com.thejunglegiant.teslastations.di

import com.thejunglegiant.teslastations.data.repository.StationsRepository
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.presentation.map.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<IStationsRepository> { StationsRepository(androidContext()) }

    viewModel { MapViewModel(get()) }
}