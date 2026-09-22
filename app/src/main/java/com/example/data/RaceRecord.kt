package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "race_records")
data class RaceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val distanceMeters: Int,
    val maxSpeedKmh: Int,
    val coinsEarned: Int,
    val nearMisses: Int,
    val carName: String,
    val gameMode: String,
    val timestamp: Long = System.currentTimeMillis()
)
