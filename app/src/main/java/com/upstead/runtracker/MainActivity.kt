package com.upstead.runtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.room.Room
import com.upstead.runtracker.data.backup.BackupManager
import com.upstead.runtracker.data.local.AppDatabase
import com.upstead.runtracker.data.repository.RunTrackerRepository
import com.upstead.runtracker.data.settings.AppSettingsStore
import com.upstead.runtracker.ui.navigation.RunTrackerNav
import com.upstead.runtracker.ui.theme.RunTrackerTheme
import com.upstead.runtracker.viewmodel.MainViewModel
import com.upstead.runtracker.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "runtracker_prefs"
        private const val PREF_DARK_MODE = "pref_dark_mode"
    }

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    private val settingsStore by lazy { AppSettingsStore(prefs) }

    private val repository by lazy {
        RunTrackerRepository(
            profileDao = database.profileDao(),
            runEntryDao = database.runEntryDao()
        )
    }

    private val backupManager by lazy { BackupManager(repository) }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository, backupManager, settingsStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkModeEnabled by remember {
                mutableStateOf(
                    if (prefs.contains(PREF_DARK_MODE)) {
                        prefs.getBoolean(PREF_DARK_MODE, false)
                    } else {
                        false
                    }
                )
            }

            RunTrackerTheme(darkTheme = darkModeEnabled) {
                RunTrackerNav(
                    viewModel = mainViewModel,
                    darkModeEnabled = darkModeEnabled,
                    onDarkModeChange = { enabled ->
                        darkModeEnabled = enabled
                        prefs.edit().putBoolean(PREF_DARK_MODE, enabled).apply()
                    }
                )
            }
        }
    }
}
