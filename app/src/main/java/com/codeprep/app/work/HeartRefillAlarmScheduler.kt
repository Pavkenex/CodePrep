package com.codeprep.app.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRefillAlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(triggerAtMillis: Long, userId: String) {
        val pendingIntent = pendingIntent(userId, PendingIntent.FLAG_UPDATE_CURRENT) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel() {
        val pendingIntent = pendingIntent(userId = null, PendingIntent.FLAG_NO_CREATE) ?: return
        alarmManager.cancel(pendingIntent)
    }

    private fun pendingIntent(userId: String?, flags: Int): PendingIntent? {
        val intent = Intent(context, HeartRefillAlarmReceiver::class.java)
            .setAction(WorkConstants.HEART_REFILL_ACTION)
        if (userId != null) {
            intent.putExtra(WorkConstants.USER_ID_KEY, userId)
        }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val REQUEST_CODE = 0x48454152
    }
}
