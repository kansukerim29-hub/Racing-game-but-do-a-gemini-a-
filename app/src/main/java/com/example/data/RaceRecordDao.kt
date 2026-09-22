package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceRecordDao {
    @Query("SELECT * FROM race_records ORDER BY score DESC LIMIT :limit")
    fun getTopRecords(limit: Int = 10): Flow<List<RaceRecord>>

    @Query("SELECT MAX(score) FROM race_records")
    fun getHighScore(): Flow<Int?>

    @Query("SELECT SUM(coinsEarned) FROM race_records")
    fun getTotalCoinsEarned(): Flow<Int?>

    @Query("SELECT SUM(distanceMeters) FROM race_records")
    fun getTotalDistanceDriven(): Flow<Int?>

    @Insert
    suspend fun insertRecord(record: RaceRecord): Long
}
