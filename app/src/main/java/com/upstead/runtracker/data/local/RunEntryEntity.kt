package com.upstead.runtracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "run_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class RunEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val weightKg: Double,
    val distanceKm: Double,
    val durationSeconds: Int,
    val notes: String?
)
