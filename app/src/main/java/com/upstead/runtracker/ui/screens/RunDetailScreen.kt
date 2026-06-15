package com.upstead.runtracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.upstead.runtracker.model.Profile
import com.upstead.runtracker.model.RunEntry
import com.upstead.runtracker.util.bmi
import com.upstead.runtracker.util.formatDuration
import com.upstead.runtracker.util.speedKmPerHour
import com.upstead.runtracker.util.toDisplayDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunDetailScreen(
    runEntry: RunEntry?,
    profile: Profile?,
    onBack: () -> Unit,
    onEdit: (date: String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Run Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (runEntry == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Run entry not found")
            }
            return@Scaffold
        }

        val avgSpeed = speedKmPerHour(runEntry.distanceKm, runEntry.durationSeconds)
        val bmiValue = profile?.let { bmi(runEntry.weightKg, it.heightCm) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(runEntry.date.toDisplayDate(), style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Weight: ${"%.1f".format(runEntry.weightKg)} kg",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Distance: ${"%.2f".format(runEntry.distanceKm)} km",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Duration: ${formatDuration(runEntry.durationSeconds)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Average speed: ${"%.2f".format(avgSpeed)} km/h",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (bmiValue != null) {
                        Text(
                            text = "BMI: ${"%.2f".format(bmiValue)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (!runEntry.notes.isNullOrBlank()) {
                        Text(
                            text = "Notes: ${runEntry.notes}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            TextButton(
                onClick = { onEdit(runEntry.date) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Edit")
            }
        }
    }
}
