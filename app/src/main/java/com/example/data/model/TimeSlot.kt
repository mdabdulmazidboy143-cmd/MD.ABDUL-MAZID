package com.example.data.model

enum class TimeSlot(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val defaultHour: Int,
    val defaultMinute: Int,
    val iconName: String
) {
    MORNING(
        id = "MORNING",
        titleBn = "সকাল",
        titleEn = "Morning",
        defaultHour = 8,
        defaultMinute = 0,
        iconName = "wb_sunny"
    ),
    NOON(
        id = "NOON",
        titleBn = "দুপুর",
        titleEn = "Noon",
        defaultHour = 13,
        defaultMinute = 30,
        iconName = "light_mode"
    ),
    EVENING(
        id = "EVENING",
        titleBn = "সন্ধ্যা",
        titleEn = "Evening",
        defaultHour = 20,
        defaultMinute = 30,
        iconName = "nights_stay"
    );

    companion object {
        fun fromId(id: String): TimeSlot {
            return entries.firstOrNull { it.id == id } ?: MORNING
        }
    }
}
