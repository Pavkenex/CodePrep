package com.codeprep.app.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.Question
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.repository.LessonProgressRepository
import com.codeprep.app.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedIndex: Int? = null,
    val isAnswered: Boolean = false,
    val score: Int = 0,
    val xpEarned: Int = 0,
    val mistakeCount: Int = 0,
    val finished: Boolean = false,
    val userHearts: Int = 0,
    val isLoading: Boolean = true,
    val loadError: String? = null
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val lessonProgressRepository: LessonProgressRepository,
    auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
    ): ViewModel()  {


    private val lessonId:String = savedStateHandle["lessonId"]?:""
    private val userId:String = auth.currentUser?.uid ?:""

    private val quizState = MutableStateFlow(QuizUiState())

    val uiState: StateFlow<QuizUiState> = combine(
        quizState,
        userRepository.getUserProgress(userId)
    ) { state, user ->
        state.copy(userHearts = user?.hearts ?: 0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuizUiState())

    init {
        viewModelScope.launch {
            if (userId.isNotBlank()) {
                userRepository.refillHearts(userId)
            }
            loadQuestions()
        }
    }

    private fun loadQuestions() {
        if (lessonId.isBlank()) {
            quizState.update {
                it.copy(
                    isLoading = false,
                    loadError = "Nedostaje lessonId za kviz."
                )
            }
            return
        }

        viewModelScope.launch {
            val questions = courseRepository.getQuestionsForLesson(lessonId)
            quizState.update {
                it.copy(
                    questions = questions,
                    isLoading = false,
                    loadError = if (questions.isEmpty()) "Nema pitanja za ovu lekciju." else null
                )
            }
        }
    }

    fun submitAnswer(index: Int) {
        val currentState = quizState.value
        val currentQuestion = currentState.questions.getOrNull(currentState.currentIndex)?:return

        val isCorrect = index == currentQuestion.correctIndex
        if(isCorrect){
            quizState.update { it.copy(
                isAnswered = true,
                selectedIndex = index,
                score=  it.score + 1 ,
                xpEarned = it.xpEarned + 10
            ) }
        }else{
            quizState.update { state->
                state.copy(
                    isAnswered = true,
                    selectedIndex = index,
                    mistakeCount = state.mistakeCount + 1
                )
            }
            if (userId.isNotBlank()) {
                viewModelScope.launch {
                    userRepository.loseHeart(userId)
                }
            }
        }
    }

    fun nextQuestion() {
        quizState.update { state: QuizUiState ->
            val nextIndex = state.currentIndex + 1
            (if (nextIndex < state.questions.size) {
                state.copy(
                    currentIndex = nextIndex,
                    isAnswered = false,
                    selectedIndex = null
                )
            } else {
                state.copy(finished = true)
            })
        }
    }

    fun onFinishClicked() {
        val currentState = quizState.value
        val xp = currentState.xpEarned

        if (userId.isBlank()) return

        viewModelScope.launch {
            lessonProgressRepository.saveAttempt(
                userId = userId,
                lessonId = lessonId,
                score = currentState.score,
                totalQuestions = currentState.questions.size,
                mistakeCount = currentState.mistakeCount
            )

            if(xp > 0){
                userRepository.addXp(userId, xp)
            }
        }
    }

}