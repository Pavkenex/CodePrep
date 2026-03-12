package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.UserProgressEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
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

            createOrUpdateUserProfile(
                userId = user.uid,
                nickname = nickname,
                email = email
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user!!

            try {
                val doc = firestore.collection("users").document(user.uid).get().await()
                if (doc.exists()) {
                    userDao.upsert(doc.toUserProgressEntity(user.uid))
                } else {
                    createOrUpdateUserProfile(
                        userId = user.uid,
                        nickname = user.displayName?.takeIf { it.isNotBlank() }
                            ?: email.substringBefore('@'),
                        email = email
                    )
                }
            } catch (_: Exception) {
                // Firestore unreachable — ensure at least a local record exists
                val existing = userDao.getUserById(user.uid)
                if (existing == null) {
                    userDao.upsert(
                        UserProgressEntity(
                            userId = user.uid,
                            nickname = email.substringBefore('@'),
                            xp = 0, level = 1, streak = 0,
                            hearts = 5, lastHeartLostAt = null,
                            lastActiveDate = Instant.now(),
                            updatedAt = Instant.now()
                        )
                    )
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser() = auth.currentUser

    override fun logout() = auth.signOut()

    private suspend fun createOrUpdateUserProfile(
        userId: String,
        nickname: String,
        email: String,
        xp: Int = 0,
        level: Int = 1,
        streak: Int = 0,
        hearts: Int = 5,
        lastHeartLostAt: Instant? = null,
        lastActiveDate: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
    ): UserProgressEntity {
        val userProgress = UserProgressEntity(
            userId = userId,
            nickname = nickname,
            xp = xp,
            level = level,
            streak = streak,
            hearts = hearts,
            lastHeartLostAt = lastHeartLostAt,
            lastActiveDate = lastActiveDate,
            updatedAt = updatedAt
        )

        // Local insert first — this is what the UI and quiz gating depend on
        userDao.upsert(userProgress)

        // Firestore write is best-effort; don't let it block login
        try {
            firestore.collection("users").document(userId).set(
                mapOf(
                    "nickname" to nickname,
                    "email" to email,
                    "xp" to xp,
                    "level" to level,
                    "streak" to streak,
                    "hearts" to hearts,
                    "lastHeartLostAt" to lastHeartLostAt,
                    "lastActiveDate" to lastActiveDate,
                    "updatedAt" to updatedAt,
                    "friends" to emptyList<String>()
                )
            ).await()
        } catch (_: Exception) {
            // Will be synced later
        }

        return userProgress
    }

    private fun DocumentSnapshot.toUserProgressEntity(userId: String): UserProgressEntity {
        return UserProgressEntity(
            userId = userId,
            nickname = getString("nickname") ?: "",
            xp = getLong("xp")?.toInt() ?: 0,
            level = getLong("level")?.toInt() ?: 1,
            streak = getLong("streak")?.toInt() ?: 0,
            hearts = getLong("hearts")?.toInt() ?: 5,
            lastHeartLostAt = getTimestamp("lastHeartLostAt")?.toInstant(),
            lastActiveDate = getTimestamp("lastActiveDate")?.toInstant() ?: Instant.now(),
            updatedAt = getTimestamp("updatedAt")?.toInstant() ?: Instant.now()
        )
    }
}