package com.codeprep.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.codeprep.app.work.WorkScheduler
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FunFactWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        context.workScheduler().enqueueFunFactWidgetRefresh()
        context.widgetUpdater().refreshInBackground()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        context.workScheduler().enqueueFunFactWidgetRefresh()
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                context.widgetUpdater().updateWidgets(appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            context.widgetUpdater().refreshInBackground()
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.workScheduler().cancelFunFactWidgetRefresh()
    }

    private fun Context.widgetUpdater(): FunFactWidgetUpdater {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            FunFactWidgetEntryPoint::class.java
        )
        return entryPoint.funFactWidgetUpdater()
    }

    private fun Context.workScheduler(): WorkScheduler {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            FunFactWidgetEntryPoint::class.java
        )
        return entryPoint.workScheduler()
    }
}
