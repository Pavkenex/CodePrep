package com.codeprep.app

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.codeprep.app.notifications.CodePrepNotificationManager
import com.codeprep.app.widget.FunFactWidgetUpdater
import com.codeprep.app.work.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CodePrepApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationManager: CodePrepNotificationManager

    @Inject
    lateinit var workScheduler: WorkScheduler

    @Inject
    lateinit var funFactWidgetUpdater: FunFactWidgetUpdater

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationManager.createChannels()
        funFactWidgetUpdater.refreshInBackground()
    }
}
