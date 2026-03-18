package com.codeprep.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codeprep.app.widget.FunFactWidgetUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FunFactWidgetRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val widgetUpdater: FunFactWidgetUpdater
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            widgetUpdater.updateAllWidgets()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
