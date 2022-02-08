package com.thejunglegiant.teslastations.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thejunglegiant.teslastations.data.database.dao.StationsDao
import com.thejunglegiant.teslastations.domain.entity.StationEntity

@Database(entities = [StationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationsDao(): StationsDao
}