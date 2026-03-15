package com.codeprep.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    userRepository: UserRepository,
    auth: FirebaseAuth
) : ViewModel() {

    private val userId = auth.currentUser?.uid.orEmpty()

    val uiState = if (userId.isBlank()) {
        flowOf(HomeUiState())
    } else {
        userRepository.getUserProgress(userId).map { progress ->
            val xp = progress?.xp ?: 0
            HomeUiState(
                nickname = progress?.nickname?.takeIf { it.isNotBlank() } ?: "Dev",
                streak = progress?.streak ?: 0,
                level = progress?.level ?: 1,
                currentLevelXp = xp % XP_PER_LEVEL,
                xpRequiredForNextLevel = XP_PER_LEVEL
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    companion object {
        private const val XP_PER_LEVEL = 500
    }
}

data class HomeUiState(
    val nickname: String = "Dev",
    val streak: Int = 0,
    val level: Int = 1,
    val currentLevelXp: Int = 0,
    val xpRequiredForNextLevel: Int = 500
)
