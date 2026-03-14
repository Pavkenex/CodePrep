package com.codeprep.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codeprep.app.data.repository.UserRepository
import com.codeprep.app.notifications.CodePrepNotificationManager
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.Instant

@HiltWorker
class StreakWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val notificationManager: CodePrepNotificationManager,
    private val reminderStateStore: StreakReminderStateStore
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(WorkConstants.USER_ID_KEY)
            ?: auth.currentUser?.uid
            ?: return Result.success()

        return try {
            userRepository.ensureLocalUserProgress(userId)
            userRepository.streakCheck(userId)

            val progress = userRepository.getUserProgressOnce(userId) ?: return Result.success()
            val lastActiveDate = progress.lastActiveDate ?: return Result.success()
            val resetAt = userRepository.calculateStreakResetTime(progress) ?: return Result.success()

            val timeUntilReset = Duration.between(Instant.now(), resetAt)
            val shouldRemind = progress.streak > 0 &&
                !timeUntilReset.isNegative &&
                timeUntilReset <= WorkConstants.STREAK_REMINDER_THRESHOLD &&
                reminderStateStore.shouldSendReminder(userId, lastActiveDate)

            if (shouldRemind) {
                notificationManager.showStreakReminderNotification()
                reminderStateStore.markReminderSent(userId, lastActiveDate)
            }

            if (timeUntilReset.isNegative || progress.streak == 0) {
                reminderStateStore.clear(userId)
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
