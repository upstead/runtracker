package com.upstead.runtracker.data.repository

import com.upstead.runtracker.data.local.ProfileDao
import com.upstead.runtracker.data.local.ProfileEntity
import com.upstead.runtracker.data.local.RunEntryDao
import com.upstead.runtracker.data.local.RunEntryEntity
import com.upstead.runtracker.model.Gender
import com.upstead.runtracker.model.Profile
import com.upstead.runtracker.model.RunEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RunTrackerRepository(
    private val profileDao: ProfileDao,
    private val runEntryDao: RunEntryDao
) {
    fun observeProfile(): Flow<Profile?> = profileDao.observeProfile().map { it?.toDomain() }

    suspend fun getProfile(): Profile? = profileDao.getProfile()?.toDomain()

    suspend fun saveProfile(profile: Profile) {
        profileDao.upsert(profile.toEntity())
    }

    fun observeRunByDate(date: String): Flow<RunEntry?> = runEntryDao.observeByDate(date).map { it?.toDomain() }

    suspend fun getRunByDate(date: String): RunEntry? = runEntryDao.getByDate(date)?.toDomain()

    fun observeRunsByDateRange(startDate: String, endDate: String): Flow<List<RunEntry>> {
        return runEntryDao.observeByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }
    }

    fun observeAllRuns(): Flow<List<RunEntry>> = runEntryDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun saveRun(entry: RunEntry) {
        runEntryDao.upsert(entry.toEntity())
    }

    suspend fun replaceAllRuns(entries: List<RunEntry>) {
        runEntryDao.replaceAll(entries.map { it.toEntity() })
    }
}

private fun ProfileEntity.toDomain() = Profile(
    id = id,
    name = name,
    heightCm = heightCm,
    gender = Gender.entries.firstOrNull { it.name == gender } ?: Gender.OTHER
)

private fun Profile.toEntity() = ProfileEntity(
    id = 0,
    name = name,
    heightCm = heightCm,
    gender = gender.name
)

private fun RunEntryEntity.toDomain() = RunEntry(
    id = id,
    date = date,
    weightKg = weightKg,
    distanceKm = distanceKm,
    durationSeconds = durationSeconds,
    notes = notes
)

private fun RunEntry.toEntity() = RunEntryEntity(
    id = id,
    date = date,
    weightKg = weightKg,
    distanceKm = distanceKm,
    durationSeconds = durationSeconds,
    notes = notes
)
