package com.upstead.runtracker.data.settings

import android.content.SharedPreferences
import com.upstead.runtracker.model.DistanceUnit
import com.upstead.runtracker.model.HeightUnit
import com.upstead.runtracker.model.UnitPreferences
import com.upstead.runtracker.model.UnitSystemMode
import com.upstead.runtracker.model.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AppSettingsStore(
    private val prefs: SharedPreferences
) {
    private val _unitPreferences = MutableStateFlow(readUnitPreferences())
    val unitPreferences: StateFlow<UnitPreferences> = _unitPreferences.asStateFlow()

    init {
        if (!prefs.contains(KEY_INSTALL_EPOCH_DAY)) {
            prefs.edit().putLong(KEY_INSTALL_EPOCH_DAY, LocalDate.now().toEpochDay()).apply()
        }
        if (!prefs.contains(KEY_LAST_BACKUP_REMINDER_EPOCH_DAY)) {
            prefs.edit().putLong(KEY_LAST_BACKUP_REMINDER_EPOCH_DAY, LocalDate.now().toEpochDay()).apply()
        }
        if (!prefs.contains(KEY_RUN_COUNT_AT_LAST_BACKUP_REMINDER)) {
            prefs.edit().putInt(KEY_RUN_COUNT_AT_LAST_BACKUP_REMINDER, 0).apply()
        }
    }

    fun updateUnitPreferences(newValue: UnitPreferences) {
        prefs.edit()
            .putString(KEY_UNIT_SYSTEM_MODE, newValue.mode.name)
            .putString(KEY_CUSTOM_HEIGHT_UNIT, newValue.customHeightUnit.name)
            .putString(KEY_CUSTOM_WEIGHT_UNIT, newValue.customWeightUnit.name)
            .putString(KEY_CUSTOM_DISTANCE_UNIT, newValue.customDistanceUnit.name)
            .apply()
        _unitPreferences.value = newValue
    }

    fun shouldShowBackupReminder(totalRunCount: Int, now: LocalDate = LocalDate.now()): Boolean {
        val lastEpochDay = prefs.getLong(KEY_LAST_BACKUP_REMINDER_EPOCH_DAY, now.toEpochDay())
        val lastReminderDate = LocalDate.ofEpochDay(lastEpochDay)
        val runCountAtLastReminder = prefs.getInt(KEY_RUN_COUNT_AT_LAST_BACKUP_REMINDER, 0)
        val runsSinceLastReminder = (totalRunCount - runCountAtLastReminder).coerceAtLeast(0)
        val monthsSinceLastReminder = ChronoUnit.MONTHS.between(lastReminderDate, now)

        return runsSinceLastReminder >= RUN_THRESHOLD || monthsSinceLastReminder >= MONTH_THRESHOLD
    }

    fun markBackupReminderHandled(totalRunCount: Int, now: LocalDate = LocalDate.now()) {
        prefs.edit()
            .putLong(KEY_LAST_BACKUP_REMINDER_EPOCH_DAY, now.toEpochDay())
            .putInt(KEY_RUN_COUNT_AT_LAST_BACKUP_REMINDER, totalRunCount)
            .apply()
    }

    fun shouldShowRatingPrompt(totalRunCount: Int, now: LocalDate = LocalDate.now()): Boolean {
        if (prefs.getBoolean(KEY_RATING_PROMPTED_SUCCESSFULLY, false)) return false
        if (prefs.getBoolean(KEY_RATING_DONT_ASK_AGAIN, false)) return false

        val installEpochDay = prefs.getLong(KEY_INSTALL_EPOCH_DAY, now.toEpochDay())
        val installDate = LocalDate.ofEpochDay(installEpochDay)
        val installedForDays = ChronoUnit.DAYS.between(installDate, now)
        if (installedForDays < RATING_INSTALL_DAYS_THRESHOLD) return false
        if (totalRunCount < RATING_RUNS_THRESHOLD) return false

        if (!prefs.contains(KEY_RATING_LAST_MAYBE_LATER_EPOCH_DAY)) return true

        val lastMaybeLaterDate = LocalDate.ofEpochDay(
            prefs.getLong(KEY_RATING_LAST_MAYBE_LATER_EPOCH_DAY, now.toEpochDay())
        )
        val runCountAtMaybeLater = prefs.getInt(KEY_RATING_RUN_COUNT_AT_MAYBE_LATER, totalRunCount)
        val daysSinceMaybeLater = ChronoUnit.DAYS.between(lastMaybeLaterDate, now)
        val runsSinceMaybeLater = (totalRunCount - runCountAtMaybeLater).coerceAtLeast(0)

        return daysSinceMaybeLater >= RATING_RETRY_DAYS_THRESHOLD || runsSinceMaybeLater >= RATING_RETRY_RUNS_THRESHOLD
    }

    fun markRatingPromptedSuccessfully() {
        prefs.edit().putBoolean(KEY_RATING_PROMPTED_SUCCESSFULLY, true).apply()
    }

    fun markRatingMaybeLater(totalRunCount: Int, now: LocalDate = LocalDate.now()) {
        prefs.edit()
            .putLong(KEY_RATING_LAST_MAYBE_LATER_EPOCH_DAY, now.toEpochDay())
            .putInt(KEY_RATING_RUN_COUNT_AT_MAYBE_LATER, totalRunCount)
            .apply()
    }

    fun markRatingDontAskAgain() {
        prefs.edit().putBoolean(KEY_RATING_DONT_ASK_AGAIN, true).apply()
    }

    private fun readUnitPreferences(): UnitPreferences {
        val mode = enumValueOrDefault(
            prefs.getString(KEY_UNIT_SYSTEM_MODE, null),
            UnitSystemMode.METRIC
        )
        val customHeightUnit = enumValueOrDefault(
            prefs.getString(KEY_CUSTOM_HEIGHT_UNIT, null),
            HeightUnit.CM
        )
        val customWeightUnit = enumValueOrDefault(
            prefs.getString(KEY_CUSTOM_WEIGHT_UNIT, null),
            WeightUnit.KG
        )
        val customDistanceUnit = enumValueOrDefault(
            prefs.getString(KEY_CUSTOM_DISTANCE_UNIT, null),
            DistanceUnit.KM
        )

        return UnitPreferences(
            mode = mode,
            customHeightUnit = customHeightUnit,
            customWeightUnit = customWeightUnit,
            customDistanceUnit = customDistanceUnit
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(rawValue: String?, defaultValue: T): T {
        return rawValue?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: defaultValue
    }

    companion object {
        private const val KEY_INSTALL_EPOCH_DAY = "pref_install_epoch_day"

        private const val KEY_UNIT_SYSTEM_MODE = "pref_unit_system_mode"
        private const val KEY_CUSTOM_HEIGHT_UNIT = "pref_custom_height_unit"
        private const val KEY_CUSTOM_WEIGHT_UNIT = "pref_custom_weight_unit"
        private const val KEY_CUSTOM_DISTANCE_UNIT = "pref_custom_distance_unit"

        private const val KEY_LAST_BACKUP_REMINDER_EPOCH_DAY = "pref_last_backup_reminder_epoch_day"
        private const val KEY_RUN_COUNT_AT_LAST_BACKUP_REMINDER = "pref_run_count_last_backup_reminder"

        private const val KEY_RATING_PROMPTED_SUCCESSFULLY = "pref_rating_prompted_successfully"
        private const val KEY_RATING_DONT_ASK_AGAIN = "pref_rating_dont_ask_again"
        private const val KEY_RATING_LAST_MAYBE_LATER_EPOCH_DAY = "pref_rating_last_maybe_later_epoch_day"
        private const val KEY_RATING_RUN_COUNT_AT_MAYBE_LATER = "pref_rating_run_count_at_maybe_later"

        private const val RUN_THRESHOLD = 100
        private const val MONTH_THRESHOLD = 6L

        private const val RATING_INSTALL_DAYS_THRESHOLD = 7L
        private const val RATING_RUNS_THRESHOLD = 5
        private const val RATING_RETRY_DAYS_THRESHOLD = 30L
        private const val RATING_RETRY_RUNS_THRESHOLD = 25
    }
}
