package com.thejunglegiant.teslastations.di

import androidx.room.Room
import com.thejunglegiant.teslastations.data.database.AppDatabase
import com.thejunglegiant.teslastations.utils.DB_NAME
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val dbModule = module {

    single {
        Room
            .databaseBuilder(androidApplication(), AppDatabase::class.java, DB_NAME)
            .build()
    }

    single { get<AppDatabase>().stationsDao() }

    single { get<AppDatabase>().regionsDao() }
}
