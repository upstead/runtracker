package com.upstead.runtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RunEntryDao {
    @Query("SELECT * FROM run_entries WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<RunEntryEntity?>

    @Query("SELECT * FROM run_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): RunEntryEntity?

    @Query("SELECT * FROM run_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeByDateRange(startDate: String, endDate: String): Flow<List<RunEntryEntity>>

    @Query("SELECT * FROM run_entries ORDER BY date ASC")
    fun observeAll(): Flow<List<RunEntryEntity>>

    @Upsert
    suspend fun upsert(entry: RunEntryEntity)

    @Query("DELETE FROM run_entries")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<RunEntryEntity>)

    @Transaction
    suspend fun replaceAll(entries: List<RunEntryEntity>) {
        clearAll()
        if (entries.isNotEmpty()) {
            insertAll(entries)
        }
    }
}
