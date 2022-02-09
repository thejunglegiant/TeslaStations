package com.thejunglegiant.teslastations.di

import com.thejunglegiant.teslastations.data.repository.PopulateRepository
import com.thejunglegiant.teslastations.data.repository.StationsRepository
import com.thejunglegiant.teslastations.domain.repository.IPopulateRepository
import com.thejunglegiant.teslastations.domain.repository.IStationsRepository
import com.thejunglegiant.teslastations.presentation.list.ListStationsViewModel
import com.thejunglegiant.teslastations.presentation.map.MapViewModel
import com.thejunglegiant.teslastations.presentation.splash.SplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<IStationsRepository> { StationsRepository(get(), get(), get()) }

    single<IPopulateRepository> { PopulateRepository(androidContext(), get(), get(), get()) }

    viewModel { SplashViewModel(get()) }

    viewModel { MapViewModel(get()) }

    viewModel { ListStationsViewModel(get()) }
}