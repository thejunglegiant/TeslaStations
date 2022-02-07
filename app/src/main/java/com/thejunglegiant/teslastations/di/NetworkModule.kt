package com.thejunglegiant.teslastations.di

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

val networkModule = module {

    single {
        OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    single {
        Gson()
    }
}