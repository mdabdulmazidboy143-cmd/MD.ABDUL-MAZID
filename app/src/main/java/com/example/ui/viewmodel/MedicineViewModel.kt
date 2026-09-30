package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MediReminderApp
import com.example.data.local.IntakeLogEntity
import com.example.data.local.MedicineEntity
import com.example.data.local.SlotSettingEntity
import com.example.data.model.IntakeStatus
import com.example.data.model.TimeSlot
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodaySlotState(
    val slot: TimeSlot,
    val medicines: List<MedicineEntity>,
    val status: IntakeStatus,
    val timeLabel: String,
    val isAlarmEnabled: Boolean,
    val logTimestamp: Long? = null
)

class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MediReminderApp).repository

    val todayDateString: String = repository.getTodayDateString()

    val allMedicines: StateFlow<List<MedicineEntity>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs: StateFlow<List<IntakeLogEntity>> = repository.getLogsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<IntakeLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTakenCount: StateFlow<Int> = repository.totalTakenCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMissedCount: StateFlow<Int> = repository.totalMissedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val slotSettings: StateFlow<List<SlotSettingEntity>> = repository.slotSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined state for the 3 daily slots (সকাল, দুপুর, সন্ধ্যা)
    val todaySlotsState: StateFlow<List<TodaySlotState>> = combine(
        allMedicines,
        todayLogs,
        slotSettings
    ) { meds, logs, settings ->
        val activeMeds = meds.filter { it.isActive }
        TimeSlot.entries.map { slot ->
            val slotMeds = when (slot) {
                TimeSlot.MORNING -> activeMeds.filter { it.forMorning }
                TimeSlot.NOON -> activeMeds.filter { it.forNoon }
                TimeSlot.EVENING -> activeMeds.filter { it.forEvening }
            }
            val log = logs.firstOrNull { it.slotId == slot.id }
            val status = if (log != null) IntakeStatus.fromString(log.status) else IntakeStatus.PENDING
            val setting = settings.firstOrNull { it.slotId == slot.id }
            val hour = setting?.hour ?: slot.defaultHour
            val minute = setting?.minute ?: slot.defaultMinute
            val isEnabled = setting?.isEnabled ?: true
            val timeFormatted = formatTime(hour, minute)

            TodaySlotState(
                slot = slot,
                medicines = slotMeds,
                status = status,
                timeLabel = timeFormatted,
                isAlarmEnabled = isEnabled,
                logTimestamp = log?.timestamp
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun markSlotStatus(slot: TimeSlot, status: IntakeStatus) {
        viewModelScope.launch {
            repository.recordIntakeStatus(
                date = todayDateString,
                slot = slot,
                status = status,
                notes = if (status == IntakeStatus.TAKEN) "অ্যাপ থেকে নিশ্চিতকৃত" else "মিসড চিহ্নিত"
            )
            _userMessage.value = when (status) {
                IntakeStatus.TAKEN -> "${slot.titleBn}ের ঔষধ 'খেয়েছি' হিসেবে ডাটাবেসে সংরক্ষিত হয়েছে!"
                IntakeStatus.MISSED -> "${slot.titleBn}ের ঔষধ 'খাইনি' হিসেবে সংরক্ষিত হয়েছে।"
                IntakeStatus.PENDING -> "${slot.titleBn}ের হিসেব রিসেট করা হয়েছে।"
            }
        }
    }

    fun saveMedicine(
        id: Long = 0,
        name: String,
        dosage: String,
        mealTiming: String,
        forMorning: Boolean,
        forNoon: Boolean,
        forEvening: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val entity = MedicineEntity(
                id = id,
                name = name.trim(),
                dosage = dosage.trim(),
                mealTiming = mealTiming,
                forMorning = forMorning,
                forNoon = forNoon,
                forEvening = forEvening,
                notes = notes.trim(),
                isActive = true
            )
            if (id == 0L) {
                repository.insertMedicine(entity)
                _userMessage.value = "'$name' সফলভাবে যুক্ত হয়েছে!"
            } else {
                repository.updateMedicine(entity)
                _userMessage.value = "'$name' আপডেট করা হয়েছে!"
            }
        }
    }

    fun deleteMedicine(medicine: MedicineEntity) {
        viewModelScope.launch {
            repository.deleteMedicine(medicine.id)
            _userMessage.value = "'${medicine.name}' মুছে ফেলা হয়েছে"
        }
    }

    fun toggleMedicineActive(medicine: MedicineEntity) {
        viewModelScope.launch {
            val updated = medicine.copy(isActive = !medicine.isActive)
            repository.updateMedicine(updated)
        }
    }

    fun updateSlotTime(slot: TimeSlot, hour: Int, minute: Int) {
        viewModelScope.launch {
            val current = slotSettings.value.firstOrNull { it.slotId == slot.id }
            val isEnabled = current?.isEnabled ?: true
            repository.updateSlotTime(slot.id, hour, minute, isEnabled)
            if (isEnabled) {
                NotificationHelper.scheduleSlotAlarm(getApplication(), slot, hour, minute)
            }
            _userMessage.value = "${slot.titleBn}ের সময় পরিবর্তন করে ${formatTime(hour, minute)} নির্ধারণ করা হয়েছে।"
        }
    }

    fun toggleSlotAlarm(slot: TimeSlot, isEnabled: Boolean) {
        viewModelScope.launch {
            val current = slotSettings.value.firstOrNull { it.slotId == slot.id }
            val hour = current?.hour ?: slot.defaultHour
            val minute = current?.minute ?: slot.defaultMinute
            repository.updateSlotTime(slot.id, hour, minute, isEnabled)
            if (isEnabled) {
                NotificationHelper.scheduleSlotAlarm(getApplication(), slot, hour, minute)
                _userMessage.value = "${slot.titleBn}ের নোটিফিকেশন চালু করা হয়েছে।"
            } else {
                NotificationHelper.cancelSlotAlarm(getApplication(), slot)
                _userMessage.value = "${slot.titleBn}ের নোটিফিকেশন বন্ধ করা হয়েছে।"
            }
        }
    }

    fun triggerTestNotification(slot: TimeSlot) {
        viewModelScope.launch {
            val medicines = repository.getMedicinesForSlot(slot)
            NotificationHelper.showSlotNotification(getApplication(), slot, medicines)
            _userMessage.value = "${slot.titleBn}ের টেস্ট নোটিফিকেশন পাঠানো হয়েছে! নোটিফিকেশন বারে দেখুন।"
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _userMessage.value = "সকল পূর্ববর্তী সেবনের ইতিহাস মুছে ফেলা হয়েছে।"
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val period = if (hour >= 12) "PM" else "AM"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        return String.format("%02d:%02d %s", hour12, minute, period)
    }
}
