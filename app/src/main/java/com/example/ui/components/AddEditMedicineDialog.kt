package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.MedicineEntity
import com.example.data.model.MealTiming
import com.example.data.model.TimeSlot

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMedicineDialog(
    medicineToEdit: MedicineEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dosage: String,
        mealTiming: String,
        forMorning: Boolean,
        forNoon: Boolean,
        forEvening: Boolean,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(medicineToEdit?.name ?: "") }
    var dosage by remember { mutableStateOf(medicineToEdit?.dosage ?: "১টি ট্যাবলেট") }
    var selectedMealTiming by remember {
        mutableStateOf(MealTiming.fromString(medicineToEdit?.mealTiming ?: "AFTER_MEAL"))
    }
    var forMorning by remember { mutableStateOf(medicineToEdit?.forMorning ?: true) }
    var forNoon by remember { mutableStateOf(medicineToEdit?.forNoon ?: false) }
    var forEvening by remember { mutableStateOf(medicineToEdit?.forEvening ?: true) }
    var notes by remember { mutableStateOf(medicineToEdit?.notes ?: "") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (medicineToEdit == null) "নতুন ঔষধ যোগ করুন" else "ঔষধ পরিবর্তন করুন",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("ঔষধের নাম (যেমন: নাপা, ওমিপ্রাজল)") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("দয়া করে ঔষধের নাম লিখুন") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medicine_name_input")
                )

                // Dosage
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("ডোজ / পরিমাণ (যেমন: ১টি ট্যাবলেট, ৫ মিলি)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medicine_dosage_input")
                )

                // Slots (তিন বেলা নির্বাচন)
                Column {
                    Text(
                        text = "কোন কোন বেলা খেতে হবে:*",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = forMorning,
                            onClick = { forMorning = !forMorning },
                            label = { Text(TimeSlot.MORNING.titleBn) },
                            leadingIcon = if (forMorning) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("chip_slot_morning")
                        )

                        FilterChip(
                            selected = forNoon,
                            onClick = { forNoon = !forNoon },
                            label = { Text(TimeSlot.NOON.titleBn) },
                            leadingIcon = if (forNoon) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("chip_slot_noon")
                        )

                        FilterChip(
                            selected = forEvening,
                            onClick = { forEvening = !forEvening },
                            label = { Text(TimeSlot.EVENING.titleBn) },
                            leadingIcon = if (forEvening) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("chip_slot_evening")
                        )
                    }
                    if (!forMorning && !forNoon && !forEvening) {
                        Text(
                            text = "অন্তত একটি বেলা নির্বাচন করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Meal Timing
                Column {
                    Text(
                        text = "খাবার সময় নির্দেশিকা:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MealTiming.entries.forEach { timing ->
                            FilterChip(
                                selected = selectedMealTiming == timing,
                                onClick = { selectedMealTiming = timing },
                                label = { Text(timing.labelBn) }
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("বিশেষ নির্দেশনা বা নোট (ঐচ্ছিক)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (!forMorning && !forNoon && !forEvening) {
                        forMorning = true
                    }
                    onSave(
                        name,
                        dosage,
                        selectedMealTiming.name,
                        forMorning,
                        forNoon,
                        forEvening,
                        notes
                    )
                },
                modifier = Modifier.testTag("save_medicine_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("বাতিল")
            }
        }
    )
}
