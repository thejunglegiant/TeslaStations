package com.thejunglegiant.teslastations.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thejunglegiant.teslastations.data.database.converters.StatusConverter
import com.thejunglegiant.teslastations.data.database.dao.RegionsDao
import com.thejunglegiant.teslastations.data.database.dao.StationsDao
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.CountryEntity
import com.thejunglegiant.teslastations.domain.entity.StationEntity

@Database(entities = [StationEntity::class, CountryEntity::class, ContinentEntity::class], version = 1)
@TypeConverters(StatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationsDao(): StationsDao
    abstract fun regionsDao(): RegionsDao
}