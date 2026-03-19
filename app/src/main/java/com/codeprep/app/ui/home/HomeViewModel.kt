package com.codeprep.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.R
import com.codeprep.app.data.local.entity.Question
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.repository.UserRepository
import com.codeprep.app.data.settings.AppStringProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val dailyChallengeStateStore: DailyChallengeStateStore,
    private val appStringProvider: AppStringProvider,
    auth: FirebaseAuth
) : ViewModel() {

    private val userId = auth.currentUser?.uid.orEmpty()
    private val dailyChallengeState = MutableStateFlow(DailyChallengeUiState())

    val uiState = combine(
        if (userId.isBlank()) {
            flowOf(null)
        } else {
            userRepository.getUserProgress(userId)
        },
        dailyChallengeState
    ) { progress, dailyChallenge ->
        val xp = progress?.xp ?: 0
        HomeUiState(
            nickname = progress?.nickname?.takeIf { it.isNotBlank() }
                ?: appStringProvider.get(R.string.home_default_nickname),
            streak = progress?.streak ?: 0,
            level = progress?.level ?: 1,
            currentLevelXp = xp % XP_PER_LEVEL,
            xpRequiredForNextLevel = XP_PER_LEVEL,
            dailyChallenge = dailyChallenge
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        loadDailyChallenge()
    }

    fun expandDailyChallenge() {
        dailyChallengeState.update { state ->
            if (state.question == null || state.isCompleted) state else state.copy(isExpanded = true)
        }
    }

    fun submitDailyAnswer(index: Int) {
        val state = dailyChallengeState.value
        val question = state.question ?: return
        if (state.isAnswered || state.isCompleted) return

        dailyChallengeState.update {
            it.copy(
                isExpanded = true,
                isAnswered = true,
                selectedIndex = index.coerceIn(0, question.options.lastIndex)
            )
        }
    }

    fun completeDailyChallenge() {
        val state = dailyChallengeState.value
        val question = state.question ?: return
        if (!state.isAnswered || state.isCompleted) return

        val today = LocalDate.now()
        dailyChallengeState.update {
            it.copy(
                isExpanded = false,
                isAnswered = false,
                selectedIndex = null,
                isCompleted = true
            )
        }

        viewModelScope.launch {
            if (userId.isNotBlank()) {
                userRepository.ensureLocalUserProgress(userId)
                userRepository.registerStreakActivity(userId)
            }
            val isCorrect = state.selectedIndex!=null && state.selectedIndex == question.correctIndex
            val xpReward = if (isCorrect) calculateDailyChallengeXp(question) else 0
            if (userId.isNotBlank() && xpReward > 0) {
                userRepository.addXp(userId, xpReward)
            }
            dailyChallengeStateStore.markCompleted(userId, today)
        }
    }

    private fun loadDailyChallenge() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val question = dailyChallengeStateStore.getCachedQuestion(today)
                ?: courseRepository.getDailyQuestion(today)?.also { fetchedQuestion ->
                    dailyChallengeStateStore.cacheQuestion(today, fetchedQuestion)
                }
            val isCompleted = dailyChallengeStateStore.isCompleted(userId, today)

            dailyChallengeState.update {
                it.copy(
                    question = question,
                    isLoading = false,
                    error = if (question == null) {
                        appStringProvider.get(R.string.home_daily_challenge_unavailable)
                    } else {
                        null
                    },
                    isExpanded = false,
                    selectedIndex = null,
                    isAnswered = false,
                    isCompleted = isCompleted
                )
            }
        }
    }

    companion object {
        private const val XP_PER_LEVEL = 500
    }

    private fun calculateDailyChallengeXp(question: Question): Int {
        return when (question.difficulty.lowercase()) {
            "easy" -> 10
            "medium" -> 15
            "hard" -> 20
            else -> 10
        }
    }
}

data class DailyChallengeUiState(
    val question: Question? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isExpanded: Boolean = false,
    val selectedIndex: Int? = null,
    val isAnswered: Boolean = false,
    val isCompleted: Boolean = false
) {
    val currentQuestion: Question? get() = question
}

data class HomeUiState(
    val nickname: String = "",
    val streak: Int = 0,
    val level: Int = 1,
    val currentLevelXp: Int = 0,
    val xpRequiredForNextLevel: Int = 500,
    val dailyChallenge: DailyChallengeUiState = DailyChallengeUiState()
)
