package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MedicineEntity
import com.example.data.model.IntakeStatus
import com.example.data.model.MealTiming
import com.example.data.model.TimeSlot
import com.example.ui.theme.EveningIndigo
import com.example.ui.theme.MorningGold
import com.example.ui.theme.NoonSun
import com.example.ui.viewmodel.TodaySlotState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScheduleScreen(
    slotsState: List<TodaySlotState>,
    onMarkStatus: (TimeSlot, IntakeStatus) -> Unit,
    onTestNotification: (TimeSlot) -> Unit,
    onNavigateToMedicines: () -> Unit,
    onEditTime: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSlots = 3
    val takenCount = slotsState.count { it.status == IntakeStatus.TAKEN }
    val progress = if (totalSlots > 0) takenCount.toFloat() / totalSlots.toFloat() else 0f

    val banglaDate = rememberFormattedBanglaDate()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Progress Header Card
            TodayProgressCard(
                banglaDate = banglaDate,
                takenCount = takenCount,
                totalCount = totalSlots,
                progress = progress
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "তিন বেলার ঔষধের তালিকা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "স্বয়ংক্রিয় ডাটাবেস সেভ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(slotsState, key = { it.slot.id }) { slotState ->
            SlotScheduleCard(
                slotState = slotState,
                onMarkTaken = { onMarkStatus(slotState.slot, IntakeStatus.TAKEN) },
                onMarkMissed = { onMarkStatus(slotState.slot, IntakeStatus.MISSED) },
                onResetStatus = { onMarkStatus(slotState.slot, IntakeStatus.PENDING) },
                onTestNotification = { onTestNotification(slotState.slot) },
                onEditTime = { onEditTime(slotState.slot) },
                onAddMedicine = onNavigateToMedicines
            )
        }

        item {
            // Explanatory Tip Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "সময়মত নোটিফিকেশন আসলে সরাসরি 'হ্যাঁ, খেয়েছি' বা 'খাইনি' বোতাম চাপলে সাথে সাথে ডাটাবেসে সেভ হয়ে যাবে। অ্যাপ না খুলেও নোটিফিকেশন থেকে নিশ্চিত করা যাবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TodayProgressCard(
    banglaDate: String,
    takenCount: Int,
    totalCount: Int,
    progress: Float
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_progress_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "আজকের সূচি",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = banglaDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "$totalCount বেলার মধ্যে $takenCount বেলা সম্পন্ন",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Text(
                text = if (takenCount == totalCount) {
                    "মাশাআল্লাহ! আজকের সব বেলার ঔষধ সফলভাবে খাওয়া সম্পন্ন হয়েছে! 🎉"
                } else {
                    "প্রতি বেলা ঔষধ খাওয়ার পর 'হ্যাঁ, খেয়েছি' বোতামে চাপ দিয়ে হিসাব রাখুন।"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SlotScheduleCard(
    slotState: TodaySlotState,
    onMarkTaken: () -> Unit,
    onMarkMissed: () -> Unit,
    onResetStatus: () -> Unit,
    onTestNotification: () -> Unit,
    onEditTime: () -> Unit,
    onAddMedicine: () -> Unit
) {
    val (slotColor, slotIcon) = when (slotState.slot) {
        TimeSlot.MORNING -> MorningGold to Icons.Default.WbSunny
        TimeSlot.NOON -> NoonSun to Icons.Default.LightMode
        TimeSlot.EVENING -> EveningIndigo to Icons.Default.NightsStay
    }

    val statusCardColor by animateColorAsState(
        targetValue = when (slotState.status) {
            IntakeStatus.TAKEN -> Color(0xFFECFDF5) // Light Emerald
            IntakeStatus.MISSED -> Color(0xFFFEF2F2) // Light Rose
            IntakeStatus.PENDING -> MaterialTheme.colorScheme.surface
        },
        label = "status_card_bg"
    )

    val border = when (slotState.status) {
        IntakeStatus.TAKEN -> BorderStroke(1.5.dp, Color(0xFF10B981))
        IntakeStatus.MISSED -> BorderStroke(1.5.dp, Color(0xFFEF4444))
        IntakeStatus.PENDING -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("slot_card_${slotState.slot.id.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusCardColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Slot Name, Icon, Time Badge, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(slotColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slotIcon,
                            contentDescription = null,
                            tint = slotColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = slotState.slot.titleBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = slotState.timeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Badge
                StatusPill(status = slotState.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Medicines List for this Slot
            Text(
                text = "${slotState.slot.titleBn}ের ঔষধসমূহ (${slotState.medicines.size} টি):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (slotState.medicines.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "এই বেলার জন্য এখনো কোনো ঔষধ যোগ করা হয়নি",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onAddMedicine) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("যোগ করুন", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slotState.medicines.forEach { medicine ->
                        MedicineItemRow(medicine = medicine)
                    }
                }
            }

            // Recorded timestamp if taken or missed
            if (slotState.status != IntakeStatus.PENDING && slotState.logTimestamp != null) {
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(slotState.logTimestamp))
                Text(
                    text = "ডাটাবেসে রেকর্ড হয়েছে: $timeStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (slotState.status == IntakeStatus.TAKEN) Color(0xFF047857) else Color(0xFFB91C1C)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons: "হ্যাঁ, খেয়েছি" (YES / TAKEN) / "খাইনি" (MISSED)
            if (slotState.status == IntakeStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // YES / TAKEN button
                    Button(
                        onClick = onMarkTaken,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_taken_${slotState.slot.id.lowercase()}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "হ্যাঁ, খেয়েছি",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // NO / MISSED button
                    OutlinedButton(
                        onClick = onMarkMissed,
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("btn_missed_${slotState.slot.id.lowercase()}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFDC2626)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "খাইনি",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // If already marked, allow undoing/resetting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (slotState.status == IntakeStatus.TAKEN) "সফলভাবে খাওয়া সম্পন্ন হয়েছে ✓" else "খাওয়া হয়নি হিসেবে চিহ্নিত ✗",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (slotState.status == IntakeStatus.TAKEN) Color(0xFF059669) else Color(0xFFDC2626)
                    )

                    OutlinedButton(
                        onClick = onResetStatus,
                        colors = ButtonDefaults.outlinedButtonColors(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পরিবর্তন করুন", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Quick actions: Test Notification & Change Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onTestNotification,
                    modifier = Modifier.testTag("test_notif_${slotState.slot.id.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "নোটিফিকেশন টেস্ট করুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(onClick = onEditTime) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "সময় পরিবর্তন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MedicineItemRow(medicine: MedicineEntity) {
    val mealTiming = MealTiming.fromString(medicine.mealTiming)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (medicine.notes.isNotBlank()) {
                    Text(
                        text = medicine.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dosage badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = medicine.dosage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Meal Timing badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (mealTiming == MealTiming.BEFORE_MEAL) {
                        Color(0xFFFEF3C7) // Amber
                    } else {
                        Color(0xFFE0F2FE) // Sky
                    }
                ) {
                    Text(
                        text = mealTiming.labelBn,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (mealTiming == MealTiming.BEFORE_MEAL) {
                            Color(0xFF92400E)
                        } else {
                            Color(0xFF0369A1)
                        },
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: IntakeStatus) {
    val (bgColor, textColor, label) = when (status) {
        IntakeStatus.TAKEN -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "✓ খেয়েছি")
        IntakeStatus.MISSED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "✗ খাইনি")
        IntakeStatus.PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "⏳ বাকি আছে")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun rememberFormattedBanglaDate(): String {
    val sdf = SimpleDateFormat("dd MMMM, yyyy (EEEE)", Locale.getDefault())
    return sdf.format(Date())
}
