package com.codeprep.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.codeprep.app.MainActivity
import com.codeprep.app.R
import com.codeprep.app.data.repository.FunFactRepository
import com.codeprep.app.ui.funfact.FunFactActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class FunFactWidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val funFactRepository: FunFactRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun refreshInBackground() {
        scope.launch {
            updateAllWidgets()
        }
    }

    suspend fun updateAllWidgets() {
        val manager = AppWidgetManager.getInstance(context)
        val widgetIds = manager.getAppWidgetIds(
            ComponentName(context, FunFactWidgetProvider::class.java)
        )
        if (widgetIds.isEmpty()) return

        updateWidgets(manager, widgetIds)
    }

    suspend fun updateWidgets(
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return

        val state = funFactRepository.getWidgetState(isLoggedIn = auth.currentUser != null)
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_fun_fact).apply {
                setTextViewText(R.id.widgetBadge, state.badge)
                setTextViewText(R.id.widgetFactText, state.factText)
                setTextViewText(R.id.widgetFooter, state.footer)
                setOnClickPendingIntent(
                    R.id.widgetRoot,
                    buildLaunchIntent(state.isLoggedIn, widgetId)
                )
            }
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun buildLaunchIntent(isLoggedIn: Boolean, appWidgetId: Int): PendingIntent {
        val targetIntent = if (isLoggedIn) {
            Intent(context, FunFactActivity::class.java)
        } else {
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }.apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        return PendingIntent.getActivity(
            context,
            appWidgetId,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
