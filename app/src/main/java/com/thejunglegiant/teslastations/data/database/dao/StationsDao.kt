package com.thejunglegiant.teslastations.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.google.android.gms.maps.model.LatLngBounds
import com.thejunglegiant.teslastations.domain.entity.StationEntity

@Dao
interface StationsDao {

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun getAllCount(): Int

    @Query("SELECT * FROM stations")
    suspend fun getAll(): List<StationEntity>

    @Query("SELECT * FROM stations LIMIT :limit OFFSET :offset")
    suspend fun getStationsOffset(limit: Int, offset: Int): List<StationEntity>

    @Query("SELECT * FROM stations WHERE latitude BETWEEN :fromLat AND :toLat AND longitude BETWEEN :fromLng AND :toLng LIMIT :limit OFFSET :offset")
    suspend fun getStationsOffsetByBounds(limit: Int, offset: Int, fromLat: Double, toLat: Double, fromLng: Double, toLng: Double): List<StationEntity>

    @Insert
    suspend fun insertAll(stations: List<StationEntity>)

    @Update
    suspend fun updateStation(station: StationEntity): Int
}