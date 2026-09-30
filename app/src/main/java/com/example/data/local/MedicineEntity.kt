package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String = "১টি ট্যাবলেট",
    val mealTiming: String = "AFTER_MEAL",
    val forMorning: Boolean = true,
    val forNoon: Boolean = false,
    val forEvening: Boolean = true,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
