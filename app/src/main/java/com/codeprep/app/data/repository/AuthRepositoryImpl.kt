package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.UserProgressEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserProgressDao
) : AuthRepository {
    override suspend fun register(
        nickname: String,
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!

            val now = System.currentTimeMillis()
            val userData = mapOf(
                "nickname" to nickname,
                "email" to email,
                "xp" to 0, "level" to 1, "streak" to 0,
                "hearts" to 5, "heartsLockedUntil" to null,
                "lastActiveDate" to now, "updatedAt" to now,
                "friends" to emptyList<String>()
            )
            firestore.collection("users").document(user.uid).set(userData).await()

            userDao.upsert(UserProgressEntity(
                userId = user.uid, xp=0, level = 1, streak = 0,
                nickname = nickname,
                hearts = 5,heartsLockedUntil = null,
                lastActiveDate = now, updatedAt = now
            ))

            Result.success(user)

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email,password).await()
            val user = result.user!!
            val doc = firestore.collection("users").document(user.uid).get().await()
            if(doc.exists()){
                UserProgressEntity(
                    userId = user.uid,
                    nickname = doc.getString("nickname").toString(),
                    xp = doc.getLong("xp")?.toInt() ?: 0,
                    level = doc.getLong("level")?.toInt() ?: 1,
                    streak = doc.getLong("streak")?.toInt() ?: 0,
                    hearts = doc.getLong("hearts")?.toInt() ?: 5,
                    heartsLockedUntil = doc.getLong("heartsLockedUntil"),
                    lastActiveDate = doc.getLong("lastActiveDate") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            }
            Result.success(result.user!!)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override fun getCurrentUser()= auth.currentUser



    override fun logout() = auth.signOut()
}