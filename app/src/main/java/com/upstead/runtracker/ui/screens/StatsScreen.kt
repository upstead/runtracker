package com.upstead.runtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.upstead.runtracker.model.Profile
import com.upstead.runtracker.model.RunEntry
import com.upstead.runtracker.ui.components.LineChartCard
import com.upstead.runtracker.util.bmi
import com.upstead.runtracker.util.speedKmPerHour

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    profile: Profile?,
    runs: List<RunEntry>,
    onBack: () -> Unit
) {
    val sorted = runs.sortedBy { it.date }
    val runDates = sorted.map { it.date }

    val distanceSeries = sorted.map { it.distanceKm }
    val speedSeries = sorted.map { speedKmPerHour(it.distanceKm, it.durationSeconds) }
    val weightSeries = sorted.map { it.weightKg }
    val bmiSeries = sorted.map { run ->
        val height = profile?.heightCm ?: 0
        bmi(run.weightKg, height)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LineChartCard(
                title = "Distance Progression",
                points = distanceSeries,
                dates = runDates,
                latestLabel = distanceSeries.lastOrNull()?.let { "Latest: ${"%.2f".format(it)} km" } ?: "No data",
                valueFormatter = { "${"%.2f".format(it)} km" }
            )
            LineChartCard(
                title = "Speed Progression",
                points = speedSeries,
                dates = runDates,
                latestLabel = speedSeries.lastOrNull()?.let { "Latest: ${"%.2f".format(it)} km/h" } ?: "No data",
                valueFormatter = { "${"%.2f".format(it)} km/h" }
            )
            LineChartCard(
                title = "Weight Progression",
                points = weightSeries,
                dates = runDates,
                latestLabel = weightSeries.lastOrNull()?.let { "Latest: ${"%.1f".format(it)} kg" } ?: "No data",
                valueFormatter = { "${"%.1f".format(it)} kg" }
            )
            LineChartCard(
                title = "BMI Progression",
                points = bmiSeries,
                dates = runDates,
                latestLabel = bmiSeries.lastOrNull()?.let { "Latest: ${"%.2f".format(it)}" } ?: "No data",
                valueFormatter = { "${"%.2f".format(it)}" }
            )
        }
    }
}
