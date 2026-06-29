package com.upstead.runtracker.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val profile: BackupProfile?,
    val runs: List<BackupRun>
)

@Serializable
data class BackupProfile(
    val name: String,
    val heightCm: Int,
    val gender: String
)

@Serializable
data class BackupRun(
    val date: String,
    val weightKg: Double,
    val distanceKm: Double,
    val durationSeconds: Int,
    val notes: String?,
    val runType: String? = null
)
