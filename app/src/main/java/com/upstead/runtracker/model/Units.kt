package com.upstead.runtracker.model

enum class UnitSystemMode {
    METRIC,
    IMPERIAL,
    CUSTOM
}

enum class HeightUnit {
    CM,
    FT_IN
}

enum class WeightUnit {
    KG,
    LB
}

enum class DistanceUnit {
    KM,
    MILES
}

data class UnitPreferences(
    val mode: UnitSystemMode = UnitSystemMode.METRIC,
    val customHeightUnit: HeightUnit = HeightUnit.CM,
    val customWeightUnit: WeightUnit = WeightUnit.KG,
    val customDistanceUnit: DistanceUnit = DistanceUnit.KM
) {
    val heightUnit: HeightUnit
        get() = when (mode) {
            UnitSystemMode.METRIC -> HeightUnit.CM
            UnitSystemMode.IMPERIAL -> HeightUnit.FT_IN
            UnitSystemMode.CUSTOM -> customHeightUnit
        }

    val weightUnit: WeightUnit
        get() = when (mode) {
            UnitSystemMode.METRIC -> WeightUnit.KG
            UnitSystemMode.IMPERIAL -> WeightUnit.LB
            UnitSystemMode.CUSTOM -> customWeightUnit
        }

    val distanceUnit: DistanceUnit
        get() = when (mode) {
            UnitSystemMode.METRIC -> DistanceUnit.KM
            UnitSystemMode.IMPERIAL -> DistanceUnit.MILES
            UnitSystemMode.CUSTOM -> customDistanceUnit
        }
}
