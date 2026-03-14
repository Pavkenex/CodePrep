package com.codeprep.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.UserProgressEntity
import com.codeprep.app.data.repository.UserRepository
import com.codeprep.app.work.WorkScheduler
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SessionBootstrapViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val workScheduler: WorkScheduler
) : ViewModel() {

    private var refreshedUserId: String? = null
    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    val currentUserProgress: StateFlow<UserProgressEntity?> = currentUserId
        .flatMapLatest { userId ->
            if (userId.isNullOrBlank()) flowOf(null) else userRepository.getUserProgress(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            refreshedUserId = null
        }
        syncSessionWorkers(userId)
        _currentUserId.value = userId
    }

    init {
        auth.addAuthStateListener(authStateListener)
        syncSessionWorkers(auth.currentUser?.uid)
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

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}
