package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.model.TimeSlot
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slotId = intent.getStringExtra(NotificationHelper.EXTRA_SLOT_ID) ?: TimeSlot.MORNING.id
        val slot = TimeSlot.fromId(slotId)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val medicines = when (slot) {
                    TimeSlot.MORNING -> db.medicineDao().getMorningMedicinesList()
                    TimeSlot.NOON -> db.medicineDao().getNoonMedicinesList()
                    TimeSlot.EVENING -> db.medicineDao().getEveningMedicinesList()
                }

                // Show the rich notification with YES/NO actions
                NotificationHelper.showSlotNotification(context, slot, medicines)

                // Reschedule for next day based on slot setting
                val setting = db.slotSettingDao().getSetting(slot.id)
                val hour = setting?.hour ?: slot.defaultHour
                val minute = setting?.minute ?: slot.defaultMinute
                if (setting?.isEnabled != false) {
                    NotificationHelper.scheduleSlotAlarm(context, slot, hour, minute)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
