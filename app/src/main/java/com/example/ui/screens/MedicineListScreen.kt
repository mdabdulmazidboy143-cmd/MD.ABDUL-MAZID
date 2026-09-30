package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MedicineEntity
import com.example.data.model.MealTiming
import com.example.data.model.TimeSlot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    medicines: List<MedicineEntity>,
    onAddMedicine: () -> Unit,
    onEditMedicine: (MedicineEntity) -> Unit,
    onDeleteMedicine: (MedicineEntity) -> Unit,
    onToggleActive: (MedicineEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("সকল", "সকাল", "দুপুর", "সন্ধ্যা")

    var medicineToDelete by remember { mutableStateOf<MedicineEntity?>(null) }

    val filteredMedicines = remember(medicines, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> medicines.filter { it.forMorning }
            2 -> medicines.filter { it.forNoon }
            3 -> medicines.filter { it.forEvening }
            else -> medicines
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_medicine")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ঔষধ যোগ করুন")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ঔষধ যোগ করুন")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Tabs
            TabRow(
                selectedTabIndex = selectedFilterIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                filterTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index },
                        text = {
                            val count = when (index) {
                                1 -> medicines.count { it.forMorning }
                                2 -> medicines.count { it.forNoon }
                                3 -> medicines.count { it.forEvening }
                                else -> medicines.size
                            }
                            Text("$title ($count)")
                        }
                    )
                }
            }

            if (filteredMedicines.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "কোনো ঔষধ পাওয়া যায়নি",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "নতুন ঔষধ যোগ করতে নিচের বোতামে চাপ দিন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMedicines, key = { it.id }) { medicine ->
                        MedicineConfigCard(
                            medicine = medicine,
                            onEdit = { onEditMedicine(medicine) },
                            onDelete = { medicineToDelete = medicine },
                            onToggleActive = { onToggleActive(medicine) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }

    medicineToDelete?.let { med ->
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            title = { Text("ঔষধ মুছে ফেলবেন?") },
            text = { Text("'${med.name}' মুছে ফেললে এটি আর রিমাইন্ডার সূচিতে অন্তর্ভুক্ত থাকবে না।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMedicine(med)
                        medicineToDelete = null
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { medicineToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicineConfigCard(
    medicine: MedicineEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    val mealTiming = MealTiming.fromString(medicine.mealTiming)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medicine_card_${medicine.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (medicine.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (medicine.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "ডোজ: ${medicine.dosage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = medicine.isActive,
                        onCheckedChange = { onToggleActive() },
                        modifier = Modifier.testTag("switch_active_${medicine.id}")
                    )
                }
            }

            // Slot Badges & Meal Timing
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (medicine.forMorning) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "সকাল",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (medicine.forNoon) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFEDD5)
                    ) {
                        Text(
                            text = "দুপুর",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFC2410C),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (medicine.forEvening) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE0E7FF)
                    ) {
                        Text(
                            text = "সন্ধ্যা",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4338CA),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = mealTiming.labelBn,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (medicine.notes.isNotBlank()) {
                Text(
                    text = "নোট: ${medicine.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "সম্পাদনা",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "মুছে ফেলুন",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
