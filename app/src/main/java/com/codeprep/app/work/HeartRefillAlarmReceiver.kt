package com.codeprep.app.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeartRefillAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var heartRefillNotifier: HeartRefillNotifier

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WorkConstants.HEART_REFILL_ACTION) return
        val userId = intent.getStringExtra(WorkConstants.USER_ID_KEY) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                heartRefillNotifier.refillAndNotify(userId)
            } catch (_: Exception) {
                // The WorkManager fallback will recover on the next app session.
            } finally {
                pendingResult.finish()
            }
        }
    }
}
