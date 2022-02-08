package com.thejunglegiant.teslastations.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.thejunglegiant.teslastations.domain.entity.StationEntity

@Dao
interface StationsDao {

    @Query("SELECT * FROM station")
    suspend fun getAll(): List<StationEntity>

    @Insert
    suspend fun insertAll(stations: List<StationEntity>)
}