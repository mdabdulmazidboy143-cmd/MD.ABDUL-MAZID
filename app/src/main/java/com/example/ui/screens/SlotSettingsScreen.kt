package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SlotSettingEntity
import com.example.data.model.TimeSlot
import com.example.ui.theme.EveningIndigo
import com.example.ui.theme.MorningGold
import com.example.ui.theme.NoonSun

@Composable
fun SlotSettingsScreen(
    slotSettings: List<SlotSettingEntity>,
    onEditSlotTime: (TimeSlot) -> Unit,
    onToggleAlarm: (TimeSlot, Boolean) -> Unit,
    onTestNotification: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "তিন বেলার সময় ও নোটিফিকেশন সেটিংস",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "প্রতিটি বেলার জন্য নির্ধারিত সময় পরিবর্তন ও নোটিফিকেশন নিয়ন্ত্রণ করুন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TimeSlot.entries.forEach { slot ->
            item(key = slot.id) {
                val setting = slotSettings.firstOrNull { it.slotId == slot.id }
                val hour = setting?.hour ?: slot.defaultHour
                val minute = setting?.minute ?: slot.defaultMinute
                val isEnabled = setting?.isEnabled ?: true

                SlotSettingCard(
                    slot = slot,
                    hour = hour,
                    minute = minute,
                    isEnabled = isEnabled,
                    onEditTime = { onEditSlotTime(slot) },
                    onToggleEnabled = { onToggleAlarm(slot, it) },
                    onTestNotification = { onTestNotification(slot) }
                )
            }
        }

        item {
            // Detailed Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "অ্যাপ ব্যবহারের নিয়মাবলী",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "১. সকাল, দুপুর এবং সন্ধ্যার নির্দিষ্ট সময়ে ফোনে নোটিফিকেশন আসবে।\n" +
                                "২. নোটিফিকেশনে ঐ বেলার সব ওষুধের তালিকা ও খাবার সময় (আগে/পরে) দেখা যাবে।\n" +
                                "৩. ওষুধ খেলে নোটিফিকেশনের 'হ্যাঁ, খেয়েছি' বোতামে চাপ দিবেন, না খেলে 'খাইনি' বোতাম চাপবেন।\n" +
                                "৪. আপনার যেকোনো সিদ্ধান্ত স্বয়ংক্রিয়ভাবে স্থানীয় ডাটাবেসে সেভ হবে এবং 'সেবনের ইতিহাস' ট্যাবে দেখা যাবে।\n" +
                                "৫. ইন্টারনেট সংযোগ ছাড়াই এই অ্যাপ সম্পূর্ণ অফলাইনে কাজ করে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SlotSettingCard(
    slot: TimeSlot,
    hour: Int,
    minute: Int,
    isEnabled: Boolean,
    onEditTime: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onTestNotification: () -> Unit
) {
    val (slotColor, slotIcon) = when (slot) {
        TimeSlot.MORNING -> MorningGold to Icons.Default.WbSunny
        TimeSlot.NOON -> NoonSun to Icons.Default.LightMode
        TimeSlot.EVENING -> EveningIndigo to Icons.Default.NightsStay
    }

    val period = if (hour >= 12) "PM" else "AM"
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    val timeFormatted = String.format("%02d:%02d %s", hour12, minute, period)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("slot_setting_${slot.id.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = slotIcon,
                        contentDescription = null,
                        tint = slotColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "${slot.titleBn}ের সময় (${slot.titleEn})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "নির্ধারিত সময়: $timeFormatted",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.testTag("switch_alarm_${slot.id.lowercase()}")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditTime,
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সময় পরিবর্তন করুন")
                }

                TextButton(onClick = onTestNotification) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নোটিফিকেশন টেস্ট")
                }
            }
        }
    }
}
