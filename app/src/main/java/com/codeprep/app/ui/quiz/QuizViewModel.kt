package com.codeprep.app.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.Question
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
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
    val finished: Boolean = false,
    val userHearts: Int = 0 // Observed from UserRepository
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
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
                    selectedIndex = index
                )
            }
            viewModelScope.launch {
                userRepository.loseHeart(userId)
            }
        }
    }

    fun nextQuestion() {
        quizState.update { state: QuizUiState ->
            val nextIndex = state.currentIndex + 1
            (if (nextIndex < state.questions.size) {
                // Ima još pitanja
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

    private fun saveXpToDatabase(xpEarned: Int){
        if(xpEarned>0){
            viewModelScope.launch {
                userRepository.addXp(userId,xpEarned)
            }
        }
    }

    fun onFinishClicked() {
        val xp = quizState.value.xpEarned
        saveXpToDatabase(xp)
        // Ovde možeš dodati i navigaciju nazad
    }


}