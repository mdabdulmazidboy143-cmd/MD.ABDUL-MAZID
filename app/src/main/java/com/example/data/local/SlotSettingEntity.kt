package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slot_settings")
data class SlotSettingEntity(
    @PrimaryKey
    val slotId: String, // MORNING, NOON, EVENING
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true
)
