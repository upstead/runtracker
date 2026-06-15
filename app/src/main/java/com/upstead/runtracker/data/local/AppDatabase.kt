package com.upstead.runtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProfileEntity::class, RunEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun runEntryDao(): RunEntryDao

    companion object {
        const val DATABASE_NAME = "runtracker.db"
    }
}
