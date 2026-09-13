package com.codeprep.app.data.repository

import com.codeprep.app.data.friends.DEFAULT_AVATAR_PRESET_ID
import com.codeprep.app.data.friends.buildNicknameSearchTerms
import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.UserProgressEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

            syncUserProfileAfterLogin(user, email)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user!!

            syncUserProfileAfterLogin(user, user.email.orEmpty())

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser() = auth.currentUser

    override fun logout() = auth.signOut()

    private suspend fun syncUserProfileAfterLogin(
        user: FirebaseUser,
        fallbackEmail: String
    ) {
        val email = user.email?.takeIf { it.isNotBlank() } ?: fallbackEmail
        val fallbackNickname = user.displayName?.takeIf { it.isNotBlank() }
            ?: email.substringBefore('@')
        val local = userDao.getUserById(user.uid)

        try {
            val doc = firestore.collection("users").document(user.uid).get().await()
            if (doc.exists()) {
                val remote = doc.toUserProgressEntity(user.uid)
                val localUpdatedAt = local?.updatedAt ?: Instant.EPOCH
                val remoteUpdatedAt = remote.updatedAt ?: Instant.EPOCH
                if (local == null || !localUpdatedAt.isAfter(remoteUpdatedAt)) {
                    userDao.upsert(remote)
                }
            } else if (local == null) {
                createOrUpdateUserProfile(
                    userId = user.uid,
                    nickname = fallbackNickname,
                    email = email
                )
            }
        } catch (_: Exception) {

            if (local == null) {
                userDao.upsert(
                    UserProgressEntity(
                        userId = user.uid,
                        nickname = fallbackNickname,
                        xp = 0, level = 1, streak = 0,
                        hearts = 5, lastHeartLostAt = null,
                        lastActiveDate = null,
                        updatedAt = Instant.now()
                    )
                )
            }
        }
    }

    private suspend fun createOrUpdateUserProfile(
        userId: String,
        nickname: String,
        email: String,
        xp: Int = 0,
        level: Int = 1,
        streak: Int = 0,
        hearts: Int = 5,
        lastHeartLostAt: Instant? = null,
        lastActiveDate: Instant? = null,
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

        userDao.upsert(userProgress)

        try {
            firestore.collection("users").document(userId).set(
                mapOf(
                    "nickname" to nickname,
                    "nicknameLower" to nickname.trim().lowercase(),
                    "nicknameSearchTerms" to buildNicknameSearchTerms(nickname),
                    "email" to email,
                    "xp" to xp,
                    "level" to level,
                    "streak" to streak,
                    "hearts" to hearts,
                    "lastHeartLostAt" to lastHeartLostAt,
                    "lastActiveDate" to lastActiveDate,
                    "updatedAt" to updatedAt,
                    "friends" to emptyList<String>(),
                    "incomingFriendRequests" to emptyList<String>(),
                    "outgoingFriendRequests" to emptyList<String>(),
                    "avatarPresetId" to DEFAULT_AVATAR_PRESET_ID,
                    "badgeIds" to emptyList<String>()
                ),
                SetOptions.merge()
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
            lastActiveDate = getTimestamp("lastActiveDate")?.toInstant(),
            updatedAt = getTimestamp("updatedAt")?.toInstant() ?: Instant.now()
        )
    }
}
