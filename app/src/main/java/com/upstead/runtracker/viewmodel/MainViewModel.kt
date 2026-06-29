package com.upstead.runtracker.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.upstead.runtracker.data.backup.BackupManager
import com.upstead.runtracker.data.repository.RunTrackerRepository
import com.upstead.runtracker.data.settings.AppSettingsStore
import com.upstead.runtracker.model.Gender
import com.upstead.runtracker.model.Profile
import com.upstead.runtracker.model.RunEntry
import com.upstead.runtracker.model.RunType
import com.upstead.runtracker.model.UnitPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class MainViewModel(
    private val repository: RunTrackerRepository,
    private val backupManager: BackupManager,
    private val settingsStore: AppSettingsStore
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    val profile = repository.observeProfile().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val allRuns = repository.observeAllRuns().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val unitPreferences = settingsStore.unitPreferences

    val monthRuns = combine(allRuns, selectedMonth) { runs, month ->
        runs.filter { run ->
            val date = LocalDate.parse(run.date)
            date.year == month.year && date.month == month.month
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedMonth.value = YearMonth.from(date)
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun goToMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun observeRun(date: String): StateFlow<RunEntry?> {
        return repository.observeRunByDate(date).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )
    }

    fun saveProfile(name: String, heightCm: Int, gender: Gender, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (name.isBlank() || heightCm <= 0) {
                _messages.emit("Please complete all required profile fields")
                return@launch
            }
            repository.saveProfile(Profile(name = name.trim(), heightCm = heightCm, gender = gender))
            onDone?.invoke()
        }
    }

    fun saveRun(
        existingId: Long,
        date: String,
        weightKg: Double,
        distanceKm: Double,
        durationSeconds: Int,
        notes: String?,
        runType: RunType,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (weightKg <= 0 || distanceKm <= 0 || durationSeconds <= 0) {
                _messages.emit("Weight, distance, and duration are required")
                return@launch
            }
            repository.saveRun(
                RunEntry(
                    id = existingId,
                    date = date,
                    weightKg = weightKg,
                    distanceKm = distanceKm,
                    durationSeconds = durationSeconds,
                    notes = notes?.trim().takeUnless { it.isNullOrBlank() },
                    runType = runType
                )
            )
            onDone?.invoke()
        }
    }

    fun updateUnitPreferences(preferences: UnitPreferences) {
        settingsStore.updateUnitPreferences(preferences)
    }

    fun shouldShowBackupReminder(totalRunCount: Int): Boolean {
        return settingsStore.shouldShowBackupReminder(totalRunCount)
    }

    fun markBackupReminderHandled(totalRunCount: Int) {
        settingsStore.markBackupReminderHandled(totalRunCount)
    }

    fun shouldShowRatingPrompt(totalRunCount: Int): Boolean {
        return settingsStore.shouldShowRatingPrompt(totalRunCount)
    }

    fun markRatingPromptedSuccessfully() {
        settingsStore.markRatingPromptedSuccessfully()
    }

    fun markRatingMaybeLater(totalRunCount: Int) {
        settingsStore.markRatingMaybeLater(totalRunCount)
    }

    fun markRatingDontAskAgain() {
        settingsStore.markRatingDontAskAgain()
    }

    fun postMessage(message: String) {
        viewModelScope.launch {
            _messages.emit(message)
        }
    }

    fun exportData(contentResolver: ContentResolver, uri: Uri, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = backupManager.exportToUri(contentResolver, uri)
            _messages.emit(if (result.isSuccess) "Data exported" else "Export failed")
            onResult?.invoke(result.isSuccess)
        }
    }

    fun importData(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.importFromUri(contentResolver, uri)
            _messages.emit(if (result.isSuccess) "Data imported" else "Import failed")
        }
    }
}

class MainViewModelFactory(
    private val repository: RunTrackerRepository,
    private val backupManager: BackupManager,
    private val settingsStore: AppSettingsStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, backupManager, settingsStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
