package com.example.data.model

enum class IntakeStatus(val labelBn: String, val labelEn: String) {
    PENDING("বাকি আছে", "Pending"),
    TAKEN("খেয়েছি", "Taken"),
    MISSED("খাইনি", "Missed");

    companion object {
        fun fromString(value: String): IntakeStatus {
            return entries.firstOrNull { it.name == value } ?: PENDING
        }
    }
}
