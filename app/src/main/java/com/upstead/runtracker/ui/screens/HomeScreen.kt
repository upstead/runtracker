package com.upstead.runtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.upstead.runtracker.model.RunEntry
import com.upstead.runtracker.model.RunType
import com.upstead.runtracker.model.UnitPreferences
import com.upstead.runtracker.ui.components.CalendarMonthView
import com.upstead.runtracker.ui.components.readableMonth
import com.upstead.runtracker.util.formatDistanceFromKm
import com.upstead.runtracker.util.formatDuration
import com.upstead.runtracker.util.formatSpeedFromKmPerHour
import com.upstead.runtracker.util.speedKmPerHour
import com.upstead.runtracker.util.toDisplayDate
import com.upstead.runtracker.util.toIsoDateString
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    month: YearMonth,
    selectedDate: LocalDate,
    monthRuns: List<RunEntry>,
    selectedDateRun: RunEntry?,
    unitPreferences: UnitPreferences,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddRunForCurrentDate: () -> Unit,
    onAddRunForSelectedDate: (String) -> Unit,
    onOpenRunDetail: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RunTracker") },
                actions = {
                    IconButton(onClick = onOpenStats) {
                        Icon(Icons.Default.BarChart, contentDescription = "Statistics")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRunForCurrentDate) {
                Icon(Icons.Default.Add, contentDescription = "Add run")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
                }
                Text(text = month.readableMonth(), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
                }
            }

            CalendarMonthView(
                month = month,
                selectedDate = selectedDate,
                markedDates = monthRuns.map { LocalDate.parse(it.date) }.toSet(),
                onDateSelected = onSelectDate,
                modifier = Modifier.padding(top = 10.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = selectedDate.toIsoDateString().toDisplayDate(),
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (selectedDateRun == null) {
                        Text(
                            text = "No run recorded",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        TextButton(
                            onClick = { onAddRunForSelectedDate(selectedDate.toIsoDateString()) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Add Run")
                        }
                    } else {
                        val avgSpeed = speedKmPerHour(selectedDateRun.distanceKm, selectedDateRun.durationSeconds)
                        Text(
                            text = "Distance: ${formatDistanceFromKm(selectedDateRun.distanceKm, unitPreferences.distanceUnit)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Duration: ${formatDuration(selectedDateRun.durationSeconds)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Avg speed: ${formatSpeedFromKmPerHour(avgSpeed, unitPreferences.distanceUnit)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Type: ${if (selectedDateRun.runType == RunType.OUTDOOR) "Outdoor" else "Treadmill"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(
                            onClick = { onOpenRunDetail(selectedDateRun.date) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("View Details")
                        }
                    }
                }
            }
        }
    }
}
