package com.upstead.runtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.upstead.runtracker.model.DistanceUnit
import com.upstead.runtracker.model.Gender
import com.upstead.runtracker.model.HeightUnit
import com.upstead.runtracker.model.Profile
import com.upstead.runtracker.model.UnitPreferences
import com.upstead.runtracker.model.UnitSystemMode
import com.upstead.runtracker.model.WeightUnit
import com.upstead.runtracker.util.heightCmToFeetInches
import com.upstead.runtracker.util.heightFeetInchesToCm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profile: Profile?,
    darkModeEnabled: Boolean,
    unitPreferences: UnitPreferences,
    onBack: () -> Unit,
    onSaveProfile: (name: String, heightCm: Int, gender: Gender) -> Unit,
    onUnitPreferencesChange: (UnitPreferences) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onExport: (uri: Uri) -> Unit,
    onImport: (uri: Uri) -> Unit,
    onOpenEmailFeedback: () -> Unit
) {
    val context = LocalContext.current
    val appVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")
    }

    var name by rememberSaveable(profile?.name) { mutableStateOf(profile?.name.orEmpty()) }
    var gender by rememberSaveable(profile?.gender) { mutableStateOf(profile?.gender ?: Gender.OTHER) }

    var heightCmText by rememberSaveable(profile?.heightCm, unitPreferences.heightUnit) { mutableStateOf("") }
    var heightFeetText by rememberSaveable(profile?.heightCm, unitPreferences.heightUnit) { mutableStateOf("") }
    var heightInchesText by rememberSaveable(profile?.heightCm, unitPreferences.heightUnit) { mutableStateOf("") }

    var expandedGender by remember { mutableStateOf(false) }
    var expandedUnitMode by remember { mutableStateOf(false) }
    var expandedCustomHeight by remember { mutableStateOf(false) }
    var expandedCustomWeight by remember { mutableStateOf(false) }
    var expandedCustomDistance by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExport(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImport(uri)
    }

    LaunchedEffect(profile?.heightCm, unitPreferences.heightUnit) {
        val cm = profile?.heightCm ?: 0
        if (unitPreferences.heightUnit == HeightUnit.CM) {
            heightCmText = if (cm > 0) cm.toString() else ""
        } else {
            val (feet, inches) = heightCmToFeetInches(cm)
            heightFeetText = if (feet > 0) feet.toString() else ""
            heightInchesText = inches.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            Text("Profile", fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (unitPreferences.heightUnit == HeightUnit.CM) {
                OutlinedTextField(
                    value = heightCmText,
                    onValueChange = { heightCmText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Height (cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = heightFeetText,
                        onValueChange = { heightFeetText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Height (ft)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightInchesText,
                        onValueChange = { heightInchesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Height (in)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedGender,
                onExpandedChange = { expandedGender = !expandedGender }
            ) {
                OutlinedTextField(
                    value = gender.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedGender,
                    onDismissRequest = { expandedGender = false }
                ) {
                    Gender.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                gender = option
                                expandedGender = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val heightCm = if (unitPreferences.heightUnit == HeightUnit.CM) {
                        heightCmText.toIntOrNull() ?: 0
                    } else {
                        val feet = heightFeetText.toIntOrNull() ?: 0
                        val inches = heightInchesText.toIntOrNull() ?: 0
                        heightFeetInchesToCm(feet, inches)
                    }
                    onSaveProfile(name, heightCm, gender)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }

            Text("Units", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedUnitMode,
                onExpandedChange = { expandedUnitMode = !expandedUnitMode }
            ) {
                OutlinedTextField(
                    value = unitModeLabel(unitPreferences.mode),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit System") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnitMode) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedUnitMode,
                    onDismissRequest = { expandedUnitMode = false }
                ) {
                    UnitSystemMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(unitModeLabel(mode)) },
                            onClick = {
                                onUnitPreferencesChange(unitPreferences.copy(mode = mode))
                                expandedUnitMode = false
                            }
                        )
                    }
                }
            }

            if (unitPreferences.mode == UnitSystemMode.CUSTOM) {
                ExposedDropdownMenuBox(
                    expanded = expandedCustomHeight,
                    onExpandedChange = { expandedCustomHeight = !expandedCustomHeight }
                ) {
                    OutlinedTextField(
                        value = if (unitPreferences.customHeightUnit == HeightUnit.CM) "cm" else "ft/in",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Height Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomHeight) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCustomHeight,
                        onDismissRequest = { expandedCustomHeight = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("cm") },
                            onClick = {
                                onUnitPreferencesChange(unitPreferences.copy(customHeightUnit = HeightUnit.CM))
                                expandedCustomHeight = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ft/in") },
                            onClick = {
                                onUnitPreferencesChange(unitPreferences.copy(customHeightUnit = HeightUnit.FT_IN))
                                expandedCustomHeight = false
                            }
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedCustomWeight,
                    onExpandedChange = { expandedCustomWeight = !expandedCustomWeight }
                ) {
                    OutlinedTextField(
                        value = if (unitPreferences.customWeightUnit == WeightUnit.KG) "kg" else "lb",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Weight Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomWeight) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCustomWeight,
                        onDismissRequest = { expandedCustomWeight = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("kg") },
                            onClick = {
                                onUnitPreferencesChange(unitPreferences.copy(customWeightUnit = WeightUnit.KG))
                                expandedCustomWeight = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("lb") },
                            onClick = {
                                onUnitPreferencesChange(unitPreferences.copy(customWeightUnit = WeightUnit.LB))
                                expandedCustomWeight = false
                            }
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedCustomDistance,
                    onExpandedChange = { expandedCustomDistance = !expandedCustomDistance }
                ) {
                    OutlinedTextField(
                        value = if (unitPreferences.customDistanceUnit == DistanceUnit.KM) "km" else "miles",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Distance Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomDistance) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCustomDistance,
                        onDismissRequest = { expandedCustomDistance = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("km") },
                            onClick = {
                                onUnitPreferencesChange(unitPreferences.copy(customDistanceUnit = DistanceUnit.KM))
                                expandedCustomDistance = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("miles") },
                            onClick = {
                                onUnitPreferencesChange(unitPreferences.copy(customDistanceUnit = DistanceUnit.MILES))
                                expandedCustomDistance = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode")
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = onDarkModeChange
                )
            }

            Button(
                onClick = { exportLauncher.launch("runtracker_backup.json") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Data")
            }

            Button(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Data")
            }

            Text("About", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            Text("RunTracker")
            Text("Version $appVersion")
            OutlinedButton(onClick = onOpenEmailFeedback, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Email, contentDescription = null)
                Text(" Powered by Upstead")
            }
        }
    }
}

private fun unitModeLabel(mode: UnitSystemMode): String {
    return when (mode) {
        UnitSystemMode.METRIC -> "Metric"
        UnitSystemMode.IMPERIAL -> "Imperial"
        UnitSystemMode.CUSTOM -> "Custom"
    }
}
