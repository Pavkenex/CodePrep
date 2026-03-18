package com.codeprep.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.UserProgressEntity
import com.codeprep.app.data.repository.LessonProgressRepository
import com.codeprep.app.data.repository.UserRepository
import com.codeprep.app.widget.FunFactWidgetUpdater
import com.codeprep.app.work.WorkScheduler
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SessionBootstrapViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val lessonProgressRepository: LessonProgressRepository,
    private val auth: FirebaseAuth,
    private val workScheduler: WorkScheduler,
    private val funFactWidgetUpdater: FunFactWidgetUpdater
) : ViewModel() {

    private var refreshedUserId: String? = null
    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    val currentUserProgress: StateFlow<UserProgressEntity?> = currentUserId
        .flatMapLatest { userId ->
            if (userId.isNullOrBlank()) flowOf(null) else userRepository.getUserProgress(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val heartRefillCountdown: StateFlow<Duration?> = currentUserId
        .flatMapLatest { userId ->
            if (userId.isNullOrBlank()) {
                flowOf<Duration?>(null)
            } else {
                userRepository.getUserProgress(userId).flatMapLatest { progress ->
                    if (progress == null) {
                        flowOf<Duration?>(null)
                    } else {
                        flow<Duration?> {
                            while (true) {
                                val remaining = userRepository.calculateTimeUntilNextHeart(progress)
                                if (remaining == null) {
                                    emit(null)
                                    return@flow
                                }

                                emit(remaining)
                                if (remaining.isZero) {
                                    userRepository.refillHearts(progress.userId)
                                    return@flow
                                }

                                delay(1_000)
                            }
                        }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            refreshedUserId = null
        }
        syncSessionWorkers(userId)
        refreshSessionData(userId)
        funFactWidgetUpdater.refreshInBackground()
        _currentUserId.value = userId
    }

    init {
        auth.addAuthStateListener(authStateListener)
        syncSessionWorkers(auth.currentUser?.uid)
        refreshSessionData(auth.currentUser?.uid)
        funFactWidgetUpdater.refreshInBackground()
    }

    fun refreshHeartsOnSessionStart() {
        val userId = currentUserId.value ?: return
        if (refreshedUserId == userId) return

        refreshedUserId = userId
        viewModelScope.launch {
            val hydrated = userRepository.ensureLocalUserProgress(
                userId = userId,
                fallbackNickname = resolveFallbackNickname()
            )
            if (hydrated) {
                userRepository.refillHearts(userId)
            }
        }
    }

    private fun resolveFallbackNickname(): String? {
        val current = auth.currentUser
        return current?.displayName?.takeIf { it.isNotBlank() }
            ?: current?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
    }

    private fun syncSessionWorkers(userId: String?) {
        viewModelScope.launch {
            if (userId.isNullOrBlank()) {
                workScheduler.cancelSessionWorkers()
            } else {
                workScheduler.enqueueSessionWorkers(userId)
            }
        }
    }

    private fun refreshSessionData(userId: String?) {
        if (userId.isNullOrBlank()) return

        viewModelScope.launch {
            try {
                userRepository.syncProgress(userId)
                lessonProgressRepository.syncProgress(userId)
            } catch (_: Exception) {
                // Background worker will retry when connectivity improves.
            }
        }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}

internal fun formatHeartRefillCountdown(remaining: Duration): String {
    val totalSeconds = remaining.seconds.coerceAtLeast(0)
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    val formatted = if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
    return formatted
}
