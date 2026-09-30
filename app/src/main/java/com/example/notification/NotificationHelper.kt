package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.MedicineEntity
import com.example.data.model.MealTiming
import com.example.data.model.TimeSlot
import com.example.receiver.MedicineAlarmReceiver
import com.example.receiver.NotificationActionReceiver
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "mediremind_med_channel"
    const val CHANNEL_NAME = "ঔষধ সেবনের রিমাইন্ডার"

    const val ACTION_TAKEN = "com.example.mediremind.ACTION_TAKEN"
    const val ACTION_MISSED = "com.example.mediremind.ACTION_MISSED"
    const val EXTRA_SLOT_ID = "extra_slot_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "সকাল, দুপুর ও সন্ধ্যার ঔষধ সেবনের অ্যালার্ট নোটিফিকেশন"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getNotificationIdForSlot(slot: TimeSlot): Int {
        return when (slot) {
            TimeSlot.MORNING -> 1001
            TimeSlot.NOON -> 1002
            TimeSlot.EVENING -> 1003
        }
    }

    fun showSlotNotification(
        context: Context,
        slot: TimeSlot,
        medicines: List<MedicineEntity>
    ) {
        createNotificationChannel(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = getNotificationIdForSlot(slot)

        // Intent to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TARGET_SLOT", slot.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: হ্যাঁ, খেয়েছি (YES / TAKEN)
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra(EXTRA_SLOT_ID, slot.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: খাইনি (NO / MISSED)
        val missedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MISSED
            putExtra(EXTRA_SLOT_ID, slot.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val missedPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            missedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "${slot.titleBn}ের ঔষধ সেবনের সময় হয়েছে!"
        val bodyBuilder = StringBuilder()
        if (medicines.isEmpty()) {
            bodyBuilder.append("${slot.titleBn}ের নির্ধারিত ঔষধগুলো গ্রহণ করুন।")
        } else {
            medicines.forEachIndexed { index, med ->
                val timingBn = MealTiming.fromString(med.mealTiming).labelBn
                bodyBuilder.append("${index + 1}. ${med.name} (${med.dosage} - $timingBn)\n")
            }
        }
        val body = bodyBuilder.toString().trimEnd()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(if (medicines.isNotEmpty()) "${medicines.size} টি ঔষধ সেবনের সময় হয়েছে" else title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.mipmap.ic_launcher, "✓ হ্যাঁ, খেয়েছি", takenPendingIntent)
            .addAction(R.mipmap.ic_launcher, "✗ খাইনি", missedPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun scheduleSlotAlarm(
        context: Context,
        slot: TimeSlot,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra(EXTRA_SLOT_ID, slot.id)
        }
        val requestCode = getNotificationIdForSlot(slot)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelSlotAlarm(context: Context, slot: TimeSlot) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MedicineAlarmReceiver::class.java)
        val requestCode = getNotificationIdForSlot(slot)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
