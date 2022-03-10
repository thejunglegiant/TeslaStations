package com.thejunglegiant.teslastations.di

import com.google.gson.Gson
import com.thejunglegiant.teslastations.data.remote.Api
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {

    single {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    single {
        Gson()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://www.tesla.com/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    single {
        provideApi(get())
    }
}

private fun provideApi(retrofit: Retrofit) = retrofit.create(Api::class.java)