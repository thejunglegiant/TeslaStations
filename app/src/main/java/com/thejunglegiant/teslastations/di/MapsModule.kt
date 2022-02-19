package com.thejunglegiant.teslastations.di

import android.location.Geocoder
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val mapsModule = module {

    single { Geocoder(androidApplication()) }
}