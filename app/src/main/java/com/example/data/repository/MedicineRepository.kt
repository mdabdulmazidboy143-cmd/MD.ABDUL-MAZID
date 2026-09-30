package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.IntakeLogEntity
import com.example.data.local.MedicineEntity
import com.example.data.local.SlotSettingEntity
import com.example.data.model.IntakeStatus
import com.example.data.model.MealTiming
import com.example.data.model.TimeSlot
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicineRepository(private val database: AppDatabase) {
    private val medicineDao = database.medicineDao()
    private val intakeLogDao = database.intakeLogDao()
    private val slotSettingDao = database.slotSettingDao()

    val allMedicines: Flow<List<MedicineEntity>> = medicineDao.getAllMedicines()
    val activeMedicines: Flow<List<MedicineEntity>> = medicineDao.getActiveMedicines()
    val morningMedicines: Flow<List<MedicineEntity>> = medicineDao.getMorningMedicines()
    val noonMedicines: Flow<List<MedicineEntity>> = medicineDao.getNoonMedicines()
    val eveningMedicines: Flow<List<MedicineEntity>> = medicineDao.getEveningMedicines()

    val allLogs: Flow<List<IntakeLogEntity>> = intakeLogDao.getAllLogs()
    val totalTakenCount: Flow<Int> = intakeLogDao.getTotalTakenCount()
    val totalMissedCount: Flow<Int> = intakeLogDao.getTotalMissedCount()
    val slotSettings: Flow<List<SlotSettingEntity>> = slotSettingDao.getAllSettings()

    fun getLogsForDate(date: String): Flow<List<IntakeLogEntity>> =
        intakeLogDao.getLogsForDate(date)

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun insertMedicine(medicine: MedicineEntity): Long =
        medicineDao.insertMedicine(medicine)

    suspend fun updateMedicine(medicine: MedicineEntity) =
        medicineDao.updateMedicine(medicine)

    suspend fun deleteMedicine(id: Long) =
        medicineDao.deleteById(id)

    suspend fun getMedicinesForSlot(slot: TimeSlot): List<MedicineEntity> {
        return when (slot) {
            TimeSlot.MORNING -> medicineDao.getMorningMedicinesList()
            TimeSlot.NOON -> medicineDao.getNoonMedicinesList()
            TimeSlot.EVENING -> medicineDao.getEveningMedicinesList()
        }
    }

    suspend fun recordIntakeStatus(
        date: String,
        slot: TimeSlot,
        status: IntakeStatus,
        notes: String = ""
    ) {
        val existingLog = intakeLogDao.getLogForDateAndSlot(date, slot.id)
        val medicines = getMedicinesForSlot(slot)
        val summary = medicines.joinToString(", ") { "${it.name} (${it.dosage})" }

        val log = existingLog?.copy(
            status = status.name,
            timestamp = System.currentTimeMillis(),
            medicinesSummary = if (summary.isNotEmpty()) summary else existingLog.medicinesSummary,
            notes = notes
        ) ?: IntakeLogEntity(
            date = date,
            slotId = slot.id,
            status = status.name,
            timestamp = System.currentTimeMillis(),
            medicinesSummary = summary,
            notes = notes
        )

        intakeLogDao.insertOrUpdateLog(log)
    }

    suspend fun updateSlotTime(slotId: String, hour: Int, minute: Int, isEnabled: Boolean = true) {
        slotSettingDao.insertOrUpdateSetting(
            SlotSettingEntity(
                slotId = slotId,
                hour = hour,
                minute = minute,
                isEnabled = isEnabled
            )
        )
    }

    suspend fun getSlotSetting(slotId: String): SlotSettingEntity? {
        return slotSettingDao.getSetting(slotId)
    }

    suspend fun clearHistory() {
        intakeLogDao.clearAllLogs()
    }

    suspend fun prepopulateDefaultDataIfNeeded() {
        if (medicineDao.getCount() == 0) {
            val sampleMedicines = listOf(
                MedicineEntity(
                    name = "প্যারাসিটামল (নাপা)",
                    dosage = "১ টি ট্যাবলেট",
                    mealTiming = MealTiming.AFTER_MEAL.name,
                    forMorning = true,
                    forNoon = true,
                    forEvening = true,
                    notes = "জ্বর বা ব্যথার জন্য"
                ),
                MedicineEntity(
                    name = "ওমেপ্রাজল ২০ মি.গ্রা. (গ্যাস্ট্রিক)",
                    dosage = "১ টি ক্যাপসুল",
                    mealTiming = MealTiming.BEFORE_MEAL.name,
                    forMorning = true,
                    forNoon = false,
                    forEvening = true,
                    notes = "খাওয়ার ২০ মিনিট আগে"
                ),
                MedicineEntity(
                    name = "ভিটামিন সি / বি-কমপ্লেক্স",
                    dosage = "১ টি ট্যাবলেট",
                    mealTiming = MealTiming.AFTER_MEAL.name,
                    forMorning = false,
                    forNoon = true,
                    forEvening = false,
                    notes = "দুপুরের খাবারের পর"
                ),
                MedicineEntity(
                    name = "প্রেসার / প্রেশারের ওষুধ",
                    dosage = "১ টি ট্যাবলেট",
                    mealTiming = MealTiming.AFTER_MEAL.name,
                    forMorning = true,
                    forNoon = false,
                    forEvening = false,
                    notes = "সকালে নাস্তার পর নিয়মিত"
                )
            )
            medicineDao.insertMedicines(sampleMedicines)
        }

        // Initialize default slot times if not present
        for (slot in TimeSlot.entries) {
            val setting = slotSettingDao.getSetting(slot.id)
            if (setting == null) {
                slotSettingDao.insertOrUpdateSetting(
                    SlotSettingEntity(
                        slotId = slot.id,
                        hour = slot.defaultHour,
                        minute = slot.defaultMinute,
                        isEnabled = true
                    )
                )
            }
        }
    }
}
