package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.model.TimeSlot
import com.example.data.repository.MedicineRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediReminderApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: MedicineRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = MedicineRepository(database)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Initialize sample data and schedule alarms
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.prepopulateDefaultDataIfNeeded()
                // Schedule alarms for all slots
                for (slot in TimeSlot.entries) {
                    val setting = repository.getSlotSetting(slot.id)
                    val hour = setting?.hour ?: slot.defaultHour
                    val minute = setting?.minute ?: slot.defaultMinute
                    if (setting?.isEnabled != false) {
                        NotificationHelper.scheduleSlotAlarm(this@MediReminderApp, slot, hour, minute)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
