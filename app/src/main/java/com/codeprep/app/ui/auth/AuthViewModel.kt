package com.codeprep.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.R
import com.codeprep.app.data.repository.AuthRepository
import com.codeprep.app.data.settings.AppStringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appStringProvider: AppStringProvider
) : ViewModel(){
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email:String, password:String){
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email,password)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: appStringProvider.get(R.string.auth_error_login)
                    )
                }
        }
    }
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.register(name, email, password)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: appStringProvider.get(R.string.auth_error_register)
                    )
                }
        }
    }
}
sealed class AuthState{
    object Idle: AuthState()
    object Loading : AuthState()
    object Success:AuthState()
    data class Error(val message:String): AuthState()
}
