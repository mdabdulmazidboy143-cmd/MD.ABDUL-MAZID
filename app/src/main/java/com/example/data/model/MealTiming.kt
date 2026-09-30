package com.example.data.model

enum class MealTiming(val labelBn: String, val labelEn: String) {
    BEFORE_MEAL("খাওয়ার আগে", "Before meal"),
    AFTER_MEAL("খাওয়ার পরে", "After meal"),
    WITH_MEAL("খাওয়ার সাথে", "With meal"),
    ANY_TIME("যেকোনো সময়", "Any time");

    companion object {
        fun fromString(value: String): MealTiming {
            return entries.firstOrNull { it.name == value } ?: AFTER_MEAL
        }
    }
}
