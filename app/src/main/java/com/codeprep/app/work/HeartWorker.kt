package com.codeprep.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HeartWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val auth: FirebaseAuth,
    private val heartRefillNotifier: HeartRefillNotifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(WorkConstants.USER_ID_KEY)
            ?: auth.currentUser?.uid
            ?: return Result.success()

        return try {
            heartRefillNotifier.refillAndNotify(userId)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
