package com.codeprep.app.work

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class StreakReminderStateStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun shouldSendReminder(userId: String, lastActiveDate: Instant): Boolean {
        val lastReminderEpoch = prefs.getLong(reminderKey(userId), Long.MIN_VALUE)
        return lastReminderEpoch != lastActiveDate.toEpochMilli()
    }

    fun markReminderSent(userId: String, lastActiveDate: Instant) {
        prefs.edit {
            putLong(reminderKey(userId), lastActiveDate.toEpochMilli())
        }
    }

    fun clear(userId: String) {
        prefs.edit()
            .remove(reminderKey(userId))
            .apply()
    }

    private fun reminderKey(userId: String): String = "streak_reminder_$userId"

    companion object {
        private const val PREFS_NAME = "worker_state"
    }
}
