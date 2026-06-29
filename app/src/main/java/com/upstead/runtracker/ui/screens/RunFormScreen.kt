package com.upstead.runtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.upstead.runtracker.model.RunEntry
import com.upstead.runtracker.model.RunType
import com.upstead.runtracker.model.UnitPreferences
import com.upstead.runtracker.model.WeightUnit
import com.upstead.runtracker.model.DistanceUnit
import com.upstead.runtracker.util.distanceFromKm
import com.upstead.runtracker.util.distanceToKm
import com.upstead.runtracker.util.parseDurationToSeconds
import com.upstead.runtracker.util.toDisplayDate
import com.upstead.runtracker.util.weightFromKg
import com.upstead.runtracker.util.weightToKg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunFormScreen(
    date: String,
    existingRun: RunEntry?,
    unitPreferences: UnitPreferences,
    onBack: () -> Unit,
    onSaveRun: (id: Long, date: String, weightKg: Double, distanceKm: Double, durationSeconds: Int, notes: String?, runType: RunType) -> Unit
) {
    var runId by rememberSaveable { mutableLongStateOf(0L) }
    var weight by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var runType by rememberSaveable { mutableStateOf(RunType.OUTDOOR) }

    LaunchedEffect(existingRun?.id) {
        if (existingRun != null) {
            runId = existingRun.id
            weight = "%.1f".format(weightFromKg(existingRun.weightKg, unitPreferences.weightUnit))
            distance = "%.2f".format(distanceFromKm(existingRun.distanceKm, unitPreferences.distanceUnit))
            val totalSeconds = existingRun.durationSeconds
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            duration = String.format("%02d:%02d", minutes, seconds)
            notes = existingRun.notes.orEmpty()
            runType = existingRun.runType
        }
    }

    val weightLabel = when (unitPreferences.weightUnit) {
        WeightUnit.KG -> "Weight (kg)"
        WeightUnit.LB -> "Weight (lb)"
    }
    val distanceLabel = when (unitPreferences.distanceUnit) {
        DistanceUnit.KM -> "Distance (km)"
        DistanceUnit.MILES -> "Distance (miles)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingRun == null) "Add Run" else "Edit Run") },
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
            Text(
                text = "Date: ${date.toDisplayDate()}",
                style = MaterialTheme.typography.titleLarge
            )

            Text("Run Type", style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RadioButton(
                    selected = runType == RunType.OUTDOOR,
                    onClick = { runType = RunType.OUTDOOR }
                )
                Text("Outdoor")
                RadioButton(
                    selected = runType == RunType.TREADMILL,
                    onClick = { runType = RunType.TREADMILL }
                )
                Text("Treadmill")
            }

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(weightLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = distance,
                onValueChange = { distance = it },
                label = { Text(distanceLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (mm:ss or hh:mm:ss)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    val durationSeconds = parseDurationToSeconds(duration) ?: 0
                    onSaveRun(
                        runId,
                        date,
                        weightToKg(weight.toDoubleOrNull() ?: 0.0, unitPreferences.weightUnit),
                        distanceToKm(distance.toDoubleOrNull() ?: 0.0, unitPreferences.distanceUnit),
                        durationSeconds,
                        notes,
                        runType
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
