package com.codeprep.app.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.codeprep.app.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class WorkScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) {
    fun enqueueFunFactWidgetRefresh() {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WorkConstants.FUN_FACT_WIDGET_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<FunFactWidgetRefreshWorker>(
                WorkConstants.FUN_FACT_WIDGET_REPEAT_INTERVAL.toHours(),
                TimeUnit.HOURS
            ).build()
        )
    }

    fun cancelFunFactWidgetRefresh() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WorkConstants.FUN_FACT_WIDGET_WORK_NAME)
    }

    suspend fun enqueueSessionWorkers(userId: String) {
        val workManager = WorkManager.getInstance(context)
        val inputData = workDataOf(WorkConstants.USER_ID_KEY to userId)

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

    suspend fun syncHeartReminder(userId: String) {
        val workManager = WorkManager.getInstance(context)
        await(workManager.cancelUniqueWork(WorkConstants.LEGACY_HEART_WORK_NAME))
        await(workManager.cancelUniqueWork(WorkConstants.HEART_REMINDER_WORK_NAME))

        val progress = userRepository.getUserProgressOnce(userId) ?: return
        val delay = HeartReminderSchedule.delayFor(progress) ?: return

        await(
            workManager.enqueueUniqueWork(
            WorkConstants.HEART_REMINDER_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<HeartWorker>()
                .setInputData(workDataOf(WorkConstants.USER_ID_KEY to userId))
                .setInitialDelay(delay.coerceAtLeast(Duration.ZERO))
                .build()
        )
        )
    }

    suspend fun cancelSessionWorkers() {
        val workManager = WorkManager.getInstance(context)
        await(workManager.cancelUniqueWork(WorkConstants.LEGACY_HEART_WORK_NAME))
        await(workManager.cancelUniqueWork(WorkConstants.HEART_REMINDER_WORK_NAME))
        await(workManager.cancelUniqueWork(WorkConstants.STREAK_WORK_NAME))
        await(workManager.cancelUniqueWork(WorkConstants.SYNC_WORK_NAME))
    }

    private suspend fun await(operation: Operation) {
        withContext(Dispatchers.IO) {
            operation.result.get()
        }
    }
}
