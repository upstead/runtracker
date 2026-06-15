package com.upstead.runtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

    val selectedDateRunFlow = remember(selectedDate) { viewModel.observeRun(selectedDate.toIsoDateString()) }
    val selectedDateRun by selectedDateRunFlow.collectAsStateWithLifecycle()

    val currentBackstack by navController.currentBackStackEntryAsState()

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
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.Settings) {
                SettingsScreen(
                    profile = profile,
                    darkModeEnabled = darkModeEnabled,
                    onBack = { navController.popBackStack() },
                    onSaveProfile = { name, heightCm, gender ->
                        viewModel.saveProfile(name, heightCm, gender)
                    },
                    onDarkModeChange = onDarkModeChange,
                    onExport = { uri ->
                        viewModel.exportData(context.contentResolver, uri)
                    },
                    onImport = { uri ->
                        viewModel.importData(context.contentResolver, uri)
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
                    onBack = { navController.popBackStack() },
                    onSaveRun = { id, runDate, weightKg, distanceKm, durationSeconds, notes ->
                        viewModel.saveRun(id, runDate, weightKg, distanceKm, durationSeconds, notes) {
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
                    onBack = { navController.popBackStack() },
                    onEdit = { editDate ->
                        navController.navigate("${Routes.RunForm}/$editDate")
                    }
                )
            }
        }
    }
}
