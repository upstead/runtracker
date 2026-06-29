package com.upstead.runtracker.ui.navigation

import android.content.Intent
import android.content.ActivityNotFoundException
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.play.core.review.ReviewManagerFactory
import com.upstead.runtracker.ui.screens.HomeScreen
import com.upstead.runtracker.ui.screens.RunDetailScreen
import com.upstead.runtracker.ui.screens.RunFormScreen
import com.upstead.runtracker.ui.screens.SettingsScreen
import com.upstead.runtracker.ui.screens.SetupScreen
import com.upstead.runtracker.ui.screens.StatsScreen
import com.upstead.runtracker.util.toIsoDateString
import com.upstead.runtracker.viewmodel.MainViewModel
import java.time.LocalDate

private object Routes {
    const val Setup = "setup"
    const val Home = "home"
    const val Stats = "stats"
    const val Settings = "settings"
    const val RunForm = "run_form"
    const val RunDetail = "run_detail"
}

@Composable
fun RunTrackerNav(
    viewModel: MainViewModel,
    darkModeEnabled: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val monthRuns by viewModel.monthRuns.collectAsStateWithLifecycle()
    val allRuns by viewModel.allRuns.collectAsStateWithLifecycle()
    val unitPreferences by viewModel.unitPreferences.collectAsStateWithLifecycle()

    val selectedDateRunFlow = remember(selectedDate) { viewModel.observeRun(selectedDate.toIsoDateString()) }
    val selectedDateRun by selectedDateRunFlow.collectAsStateWithLifecycle()
    val currentBackstack by navController.currentBackStackEntryAsState()

    var showBackupReminder by remember { mutableStateOf(false) }
    var pendingReminderExport by remember { mutableStateOf(false) }
    var showRatingPrompt by remember { mutableStateOf(false) }
    var ratingHandledThisSession by remember { mutableStateOf(false) }
    val reviewManager = remember(context) { ReviewManagerFactory.create(context) }

    val reminderExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportData(context.contentResolver, uri) { success ->
                if (success && pendingReminderExport) {
                    viewModel.markBackupReminderHandled(allRuns.size)
                }
                pendingReminderExport = false
            }
        } else {
            pendingReminderExport = false
        }
    }

    LaunchedEffect(profile, currentBackstack?.destination?.route) {
        val route = currentBackstack?.destination?.route
        if (profile == null && route != Routes.Setup) {
            navController.navigate(Routes.Setup) {
                popUpTo(Routes.Home) { inclusive = true }
            }
        }
        if (profile != null && route == Routes.Setup) {
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Setup) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(allRuns.size) {
        showBackupReminder = viewModel.shouldShowBackupReminder(allRuns.size)
    }

    LaunchedEffect(allRuns.size, currentBackstack?.destination?.route, ratingHandledThisSession, showBackupReminder) {
        val route = currentBackstack?.destination?.route
        val isHome = route == Routes.Home
        showRatingPrompt = isHome && !showBackupReminder && !ratingHandledThisSession && viewModel.shouldShowRatingPrompt(allRuns.size)
    }

    if (showBackupReminder) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Backup Reminder") },
            text = { Text("Consider exporting a backup of your RunTracker data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingReminderExport = true
                        showBackupReminder = false
                        reminderExportLauncher.launch("runtracker_backup.json")
                    }
                ) {
                    Text("Export Backup")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.markBackupReminderHandled(allRuns.size)
                        showBackupReminder = false
                    }
                ) {
                    Text("Later")
                }
            }
        )
    }

    if (showRatingPrompt) {
        AlertDialog(
            onDismissRequest = {
                viewModel.markRatingMaybeLater(allRuns.size)
                showRatingPrompt = false
                ratingHandledThisSession = true
            },
            title = { Text("Enjoying RunTracker?") },
            text = {
                Text(
                    "You've recorded over 25 runs with RunTracker.\n\n" +
                        "If the app has been useful, consider leaving a rating. It helps other runners discover the app."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val activity = context.findActivity()
                        if (activity == null) {
                            viewModel.postMessage("Unable to open rating prompt right now")
                            viewModel.markRatingMaybeLater(allRuns.size)
                            showRatingPrompt = false
                            ratingHandledThisSession = true
                            return@TextButton
                        }

                        reviewManager.requestReviewFlow().addOnCompleteListener { requestTask ->
                            if (requestTask.isSuccessful) {
                                val reviewInfo = requestTask.result
                                reviewManager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener { flowTask ->
                                    if (flowTask.isSuccessful) {
                                        viewModel.markRatingPromptedSuccessfully()
                                    }
                                }
                            } else {
                                viewModel.postMessage("Unable to open rating prompt right now")
                                viewModel.markRatingMaybeLater(allRuns.size)
                            }
                        }

                        showRatingPrompt = false
                        ratingHandledThisSession = true
                    }
                ) {
                    Text("Rate App")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            viewModel.markRatingMaybeLater(allRuns.size)
                            showRatingPrompt = false
                            ratingHandledThisSession = true
                        }
                    ) {
                        Text("Maybe Later")
                    }

                    TextButton(
                        onClick = {
                            viewModel.markRatingDontAskAgain()
                            showRatingPrompt = false
                            ratingHandledThisSession = true
                        }
                    ) {
                        Text("Don't Ask Again")
                    }
                }
            },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (profile == null) Routes.Setup else Routes.Home,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Setup) {
                SetupScreen(
                    onSaveProfile = { name, heightCm, gender ->
                        viewModel.saveProfile(name, heightCm, gender) {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Setup) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.Home) {
                HomeScreen(
                    month = month,
                    selectedDate = selectedDate,
                    monthRuns = monthRuns,
                    selectedDateRun = selectedDateRun,
                    unitPreferences = unitPreferences,
                    onPrevMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onSelectDate = { viewModel.selectDate(it) },
                    onOpenStats = { navController.navigate(Routes.Stats) },
                    onOpenSettings = { navController.navigate(Routes.Settings) },
                    onAddRunForCurrentDate = {
                        val date = LocalDate.now().toIsoDateString()
                        navController.navigate("${Routes.RunForm}/$date")
                    },
                    onAddRunForSelectedDate = { date ->
                        navController.navigate("${Routes.RunForm}/$date")
                    },
                    onOpenRunDetail = { date ->
                        navController.navigate("${Routes.RunDetail}/$date")
                    }
                )
            }

            composable(Routes.Stats) {
                StatsScreen(
                    profile = profile,
                    runs = allRuns,
                    unitPreferences = unitPreferences,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.Settings) {
                SettingsScreen(
                    profile = profile,
                    darkModeEnabled = darkModeEnabled,
                    unitPreferences = unitPreferences,
                    onBack = { navController.popBackStack() },
                    onSaveProfile = { name, heightCm, gender ->
                        viewModel.saveProfile(name, heightCm, gender)
                    },
                    onUnitPreferencesChange = { preferences ->
                        viewModel.updateUnitPreferences(preferences)
                    },
                    onDarkModeChange = onDarkModeChange,
                    onExport = { uri ->
                        viewModel.exportData(context.contentResolver, uri) { success ->
                            if (success) {
                                viewModel.markBackupReminderHandled(allRuns.size)
                            }
                        }
                    },
                    onImport = { uri ->
                        viewModel.importData(context.contentResolver, uri)
                    },
                    onOpenEmailFeedback = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:contact@upstead.ai")
                            putExtra(Intent.EXTRA_SUBJECT, "RunTracker Feedback")
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Send feedback"))
                        } catch (_: ActivityNotFoundException) {
                            viewModel.postMessage("No email app found")
                        }
                    }
                )
            }

            composable(
                route = "${Routes.RunForm}/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: return@composable
                val existingRunFlow = remember(date) { viewModel.observeRun(date) }
                val existingRun by existingRunFlow.collectAsStateWithLifecycle()

                RunFormScreen(
                    date = date,
                    existingRun = existingRun,
                    unitPreferences = unitPreferences,
                    onBack = { navController.popBackStack() },
                    onSaveRun = { id, runDate, weightKg, distanceKm, durationSeconds, notes, runType ->
                        viewModel.saveRun(id, runDate, weightKg, distanceKm, durationSeconds, notes, runType) {
                            navController.navigate("${Routes.RunDetail}/$runDate") {
                                popUpTo(Routes.Home)
                            }
                        }
                    }
                )
            }

            composable(
                route = "${Routes.RunDetail}/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: return@composable
                val runFlow = remember(date) { viewModel.observeRun(date) }
                val run by runFlow.collectAsStateWithLifecycle()

                RunDetailScreen(
                    runEntry = run,
                    profile = profile,
                    unitPreferences = unitPreferences,
                    onBack = { navController.popBackStack() },
                    onEdit = { editDate ->
                        navController.navigate("${Routes.RunForm}/$editDate")
                    }
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
