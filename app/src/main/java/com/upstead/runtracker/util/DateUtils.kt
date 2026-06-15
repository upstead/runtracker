package com.upstead.runtracker.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

fun LocalDate.toIsoDateString(): String = format(isoFormatter)

fun String.toLocalDate(): LocalDate = LocalDate.parse(this, isoFormatter)

fun String.toDisplayDate(): String = toLocalDate().format(displayFormatter)

fun monthGridDates(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val leadingBlanks = ((firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value) + 7) % 7

    val days = buildList {
        repeat(leadingBlanks) { add(null) }
        for (day in 1..daysInMonth) {
            add(month.atDay(day))
        }
    }

    val trailingBlanks = (7 - (days.size % 7)) % 7
    return days + List(trailingBlanks) { null }
}

fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

fun parseDurationToSeconds(input: String): Int? {
    val chunks = input.trim().split(':')
    return when (chunks.size) {
        2 -> {
            val minutes = chunks[0].toIntOrNull() ?: return null
            val seconds = chunks[1].toIntOrNull() ?: return null
            if (seconds !in 0..59 || minutes < 0) return null
            minutes * 60 + seconds
        }
        3 -> {
            val hours = chunks[0].toIntOrNull() ?: return null
            val minutes = chunks[1].toIntOrNull() ?: return null
            val seconds = chunks[2].toIntOrNull() ?: return null
            if (hours < 0 || minutes !in 0..59 || seconds !in 0..59) return null
            hours * 3600 + minutes * 60 + seconds
        }
        else -> null
    }
}

fun speedKmPerHour(distanceKm: Double, durationSeconds: Int): Double {
    if (durationSeconds <= 0) return 0.0
    val hours = durationSeconds / 3600.0
    return distanceKm / hours
}

fun bmi(weightKg: Double, heightCm: Int): Double {
    if (heightCm <= 0) return 0.0
    val meters = heightCm / 100.0
    return weightKg / (meters * meters)
}
