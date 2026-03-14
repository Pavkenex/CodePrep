package com.codeprep.app.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun enqueueSessionWorkers(userId: String) {
        val workManager = WorkManager.getInstance(context)
        val inputData = workDataOf(WorkConstants.USER_ID_KEY to userId)

        workManager.enqueueUniquePeriodicWork(
            WorkConstants.HEART_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<HeartWorker>(
                WorkConstants.HEART_REPEAT_INTERVAL.toMinutes(),
                TimeUnit.MINUTES
            )
                .setInputData(inputData)
                .build()
        )

        workManager.enqueueUniquePeriodicWork(
            WorkConstants.STREAK_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<StreakWorker>(
                WorkConstants.STREAK_REPEAT_INTERVAL.toMinutes(),
                TimeUnit.MINUTES
            )
                .setInputData(inputData)
                .build()
        )

        workManager.enqueueUniquePeriodicWork(
            WorkConstants.SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<SyncProgressWorker>(
                WorkConstants.SYNC_REPEAT_INTERVAL.toMinutes(),
                TimeUnit.MINUTES
            )
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        )
    }

    suspend fun cancelSessionWorkers() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WorkConstants.HEART_WORK_NAME)
        workManager.cancelUniqueWork(WorkConstants.STREAK_WORK_NAME)
        workManager.cancelUniqueWork(WorkConstants.SYNC_WORK_NAME)
    }
}
