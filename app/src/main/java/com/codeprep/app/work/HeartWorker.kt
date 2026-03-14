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

@HiltWorker
class HeartWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val notificationManager: CodePrepNotificationManager
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(WorkConstants.USER_ID_KEY)
            ?: auth.currentUser?.uid
            ?: return Result.success()

        return try {
            userRepository.ensureLocalUserProgress(userId)

            val before = userRepository.getUserProgressOnce(userId) ?: return Result.success()
            if (before.hearts >= UserRepository.MAX_HEARTS || before.lastHeartLostAt == null) {
                return Result.success()
            }

            userRepository.refillHearts(userId)
            val after = userRepository.getUserProgressOnce(userId) ?: return Result.success()

            if (before.hearts < UserRepository.MAX_HEARTS && after.hearts == UserRepository.MAX_HEARTS) {
                notificationManager.showHeartsRefilledNotification()
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
