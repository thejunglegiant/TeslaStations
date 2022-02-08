package com.thejunglegiant.teslastations

import androidx.multidex.MultiDexApplication
import com.thejunglegiant.teslastations.di.appModule
import com.thejunglegiant.teslastations.di.dbModule
import com.thejunglegiant.teslastations.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class TeslaStationApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidLogger(Level.ERROR)
            androidContext(this@TeslaStationApp)
            modules(appModule, networkModule, dbModule)
        }
    }
}