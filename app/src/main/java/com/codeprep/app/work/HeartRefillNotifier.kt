package com.codeprep.app.work

import com.codeprep.app.data.repository.UserRepository
import com.codeprep.app.notifications.CodePrepNotificationManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRefillNotifier @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationManager: CodePrepNotificationManager
) {

    suspend fun refillAndNotify(userId: String) {
        userRepository.ensureLocalUserProgress(userId)

        val before = userRepository.getUserProgressOnce(userId) ?: return
        if (before.hearts >= UserRepository.MAX_HEARTS || before.lastHeartLostAt == null) return

        userRepository.refillHearts(userId)
        val after = userRepository.getUserProgressOnce(userId) ?: return

        if (after.hearts == UserRepository.MAX_HEARTS) {
            notificationManager.showHeartsRefilledNotification()
        }
    }
}
