package com.thejunglegiant.teslastations.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.thejunglegiant.teslastations.domain.entity.StationEntity

@Dao
interface StationsDao {

    @Query("SELECT COUNT(*) FROM station")
    suspend fun getAllCount(): Int

    @Query("SELECT * FROM station")
    suspend fun getAll(): List<StationEntity>

    @Insert
    suspend fun insertAll(stations: List<StationEntity>)

    @Update
    suspend fun updateStation(station: StationEntity): Int
}