package com.codeprep.app.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun register(nickname: String, email: String, password: String): Result<FirebaseUser>
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser>
    fun getCurrentUser(): FirebaseUser?
    fun logout()
}
