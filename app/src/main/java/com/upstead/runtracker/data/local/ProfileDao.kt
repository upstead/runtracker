package com.upstead.runtracker.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Upsert
    suspend fun upsert(profile: ProfileEntity)
}
