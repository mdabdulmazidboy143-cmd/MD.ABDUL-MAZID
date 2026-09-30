package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intake_logs",
    indices = [Index(value = ["date", "slotId"], unique = true)]
)
data class IntakeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val slotId: String, // MORNING, NOON, EVENING
    val status: String, // TAKEN, MISSED, PENDING
    val timestamp: Long = System.currentTimeMillis(),
    val medicinesSummary: String = "",
    val notes: String = ""
)
