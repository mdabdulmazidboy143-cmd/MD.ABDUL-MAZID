package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.data.local.AppDatabase
import com.example.data.model.IntakeStatus
import com.example.data.model.TimeSlot
import com.example.data.repository.MedicineRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val slotId = intent.getStringExtra(NotificationHelper.EXTRA_SLOT_ID) ?: TimeSlot.MORNING.id
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 0)
        val slot = TimeSlot.fromId(slotId)

        // Dismiss notification immediately
        if (notificationId != 0) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repository = MedicineRepository(db)
                val today = repository.getTodayDateString()

                val (status, toastMessage) = when (action) {
                    NotificationHelper.ACTION_TAKEN -> {
                        IntakeStatus.TAKEN to "${slot.titleBn}ের ঔষধ সেবন ডাটাবেসে 'খেয়েছি' হিসেবে সংরক্ষিত হয়েছে! ✓"
                    }
                    NotificationHelper.ACTION_MISSED -> {
                        IntakeStatus.MISSED to "${slot.titleBn}ের ঔষধ সেবন ডাটাবেসে 'খাইনি' হিসেবে সংরক্ষিত হয়েছে।"
                    }
                    else -> {
                        IntakeStatus.PENDING to ""
                    }
                }

                repository.recordIntakeStatus(
                    date = today,
                    slot = slot,
                    status = status,
                    notes = "নোটিফিকেশন থেকে নিশ্চিতকরণ"
                )

                if (toastMessage.isNotEmpty()) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
