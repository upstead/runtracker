package com.upstead.runtracker.model

data class RunEntry(
    val id: Long = 0,
    val date: String,
    val weightKg: Double,
    val distanceKm: Double,
    val durationSeconds: Int,
    val notes: String?
)
