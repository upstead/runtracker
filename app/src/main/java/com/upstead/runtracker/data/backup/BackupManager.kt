package com.upstead.runtracker.data.backup

import android.content.ContentResolver
import android.net.Uri
import com.upstead.runtracker.data.repository.RunTrackerRepository
import com.upstead.runtracker.model.BackupPayload
import com.upstead.runtracker.model.BackupProfile
import com.upstead.runtracker.model.BackupRun
import com.upstead.runtracker.model.Gender
import com.upstead.runtracker.model.Profile
import com.upstead.runtracker.model.RunEntry
import com.upstead.runtracker.model.RunType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class BackupManager(
    private val repository: RunTrackerRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToUri(contentResolver: ContentResolver, uri: Uri): Result<Unit> {
        return runCatching {
            val profile = repository.getProfile()
            val runs = repository.observeAllRuns().first()
            val payload = BackupPayload(
                profile = profile?.let {
                    BackupProfile(
                        name = it.name,
                        heightCm = it.heightCm,
                        gender = it.gender.name
                    )
                },
                runs = runs.map {
                    BackupRun(
                        date = it.date,
                        weightKg = it.weightKg,
                        distanceKm = it.distanceKm,
                        durationSeconds = it.durationSeconds,
                        notes = it.notes,
                        runType = it.runType.name
                    )
                }
            )

            val text = json.encodeToString(BackupPayload.serializer(), payload)
            contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(text.toByteArray())
            } ?: error("Unable to open output stream")
        }
    }

    suspend fun importFromUri(contentResolver: ContentResolver, uri: Uri): Result<Unit> {
        return runCatching {
            val text = contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: error("Unable to open input stream")

            val payload = json.decodeFromString(BackupPayload.serializer(), text)

            payload.profile?.let {
                repository.saveProfile(
                    Profile(
                        name = it.name,
                        heightCm = it.heightCm,
                        gender = Gender.entries.firstOrNull { gender -> gender.name == it.gender } ?: Gender.OTHER
                    )
                )
            }

            repository.replaceAllRuns(
                payload.runs.map {
                    RunEntry(
                        date = it.date,
                        weightKg = it.weightKg,
                        distanceKm = it.distanceKm,
                        durationSeconds = it.durationSeconds,
                        notes = it.notes,
                        runType = RunType.entries.firstOrNull { type -> type.name == it.runType } ?: RunType.OUTDOOR
                    )
                }
            )
        }
    }
}
