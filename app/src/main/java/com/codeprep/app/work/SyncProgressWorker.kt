package com.codeprep.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codeprep.app.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncProgressWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(WorkConstants.USER_ID_KEY)
            ?: auth.currentUser?.uid
            ?: return Result.success()

        return try {
            userRepository.syncProgress(userId)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
