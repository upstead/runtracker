package com.upstead.runtracker.util

import com.upstead.runtracker.model.DistanceUnit
import com.upstead.runtracker.model.HeightUnit
import com.upstead.runtracker.model.WeightUnit
import kotlin.math.floor
import kotlin.math.roundToInt

private const val KG_TO_LB = 2.2046226218
private const val KM_TO_MILES = 0.6213711922
private const val CM_PER_INCH = 2.54
private const val INCHES_PER_FOOT = 12.0

fun formatWeightFromKg(weightKg: Double, unit: WeightUnit): String {
    return when (unit) {
        WeightUnit.KG -> "${"%.1f".format(weightKg)} kg"
        WeightUnit.LB -> "${"%.1f".format(weightKg * KG_TO_LB)} lb"
    }
}

fun formatDistanceFromKm(distanceKm: Double, unit: DistanceUnit): String {
    return when (unit) {
        DistanceUnit.KM -> "${"%.2f".format(distanceKm)} km"
        DistanceUnit.MILES -> "${"%.2f".format(distanceKm * KM_TO_MILES)} miles"
    }
}

fun formatSpeedFromKmPerHour(speedKmPerHour: Double, unit: DistanceUnit): String {
    return when (unit) {
        DistanceUnit.KM -> "${"%.2f".format(speedKmPerHour)} km/h"
        DistanceUnit.MILES -> "${"%.2f".format(speedKmPerHour * KM_TO_MILES)} mph"
    }
}

fun weightToKg(value: Double, unit: WeightUnit): Double {
    return when (unit) {
        WeightUnit.KG -> value
        WeightUnit.LB -> value / KG_TO_LB
    }
}

fun weightFromKg(weightKg: Double, unit: WeightUnit): Double {
    return when (unit) {
        WeightUnit.KG -> weightKg
        WeightUnit.LB -> weightKg * KG_TO_LB
    }
}

fun distanceToKm(value: Double, unit: DistanceUnit): Double {
    return when (unit) {
        DistanceUnit.KM -> value
        DistanceUnit.MILES -> value / KM_TO_MILES
    }
}

fun distanceFromKm(distanceKm: Double, unit: DistanceUnit): Double {
    return when (unit) {
        DistanceUnit.KM -> distanceKm
        DistanceUnit.MILES -> distanceKm * KM_TO_MILES
    }
}

fun heightCmToFeetInches(heightCm: Int): Pair<Int, Int> {
    val totalInches = heightCm / CM_PER_INCH
    val feet = floor(totalInches / INCHES_PER_FOOT).toInt()
    val inches = (totalInches - (feet * INCHES_PER_FOOT)).roundToInt().coerceIn(0, 11)
    return feet to inches
}

fun heightFeetInchesToCm(feet: Int, inches: Int): Int {
    val normalizedInches = inches.coerceIn(0, 11)
    val cm = ((feet * INCHES_PER_FOOT) + normalizedInches) * CM_PER_INCH
    return cm.roundToInt()
}

fun formatHeightFromCm(heightCm: Int, unit: HeightUnit): String {
    return when (unit) {
        HeightUnit.CM -> "$heightCm cm"
        HeightUnit.FT_IN -> {
            val (feet, inches) = heightCmToFeetInches(heightCm)
            "$feet ft $inches in"
        }
    }
}
