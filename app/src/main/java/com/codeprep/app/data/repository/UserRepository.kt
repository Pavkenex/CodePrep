package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.UserProgressEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserProgressDao,
    private val firestore: FirebaseFirestore
) {

    //Za UI
    fun getUserProgress(userId: String): Flow<UserProgressEntity?> {
        return userDao.getUserProgressStream(userId)
    }

    suspend fun getUserProgressOnce(userId: String): UserProgressEntity? {
        return userDao.getUserById(userId)
    }

    // 2. HELPER FUNKCIJA ZA AŽURIRANJE
    suspend fun updateProgress(
        userId: String,
        transform: (UserProgressEntity) -> UserProgressEntity
    ) {

        val current = userDao.getUserById(userId) ?: return

        val updated = transform(current).copy(
            updatedAt = Instant.now()
        )

        userDao.upsert(updated)

       //TODO Ova linija ce biti uklonjena jer ce worker sinhronizovati u odredjenom periodu sa bazom kako bi se smanjio broj zahteva ka firebase-u
        firestore.collection("users").document(userId).set(updated)
    }


    suspend fun addXp(userId: String, amount: Int) {
        updateProgress(userId) { progress ->
            val newXp = progress.xp + amount
            // Logika za level up
            progress.copy(
                xp = newXp,
                level = (newXp / 500) + 1
            )
        }
    }

    suspend fun loseHeart(userId: String) {
        updateProgress(userId) { progress ->
            val newHearts = (progress.hearts - 1).coerceAtLeast(0)

            progress.copy(
                hearts = newHearts,
                lastHeartLostAt = progress.lastHeartLostAt ?: Instant.now()
            )
        }
    }

    suspend fun refillHearts(userId: String) {
        updateProgress(userId) { user ->

            if (user.hearts >= 5 || user.lastHeartLostAt == null) {
                user
            } else {
                val minutesPassed =
                    ChronoUnit.MINUTES.between(user.lastHeartLostAt, Instant.now())


                val heartsToAdd = (minutesPassed / 30).toInt()
                val newHearts = (user.hearts + heartsToAdd).coerceAtMost(5)
                if(newHearts<5){
                    val updatedLastHeartLostAt = user.lastHeartLostAt.plus(
                        (heartsToAdd*30).toLong(),
                        ChronoUnit.MINUTES)
                    user.copy(
                        hearts = newHearts,
                        lastHeartLostAt = updatedLastHeartLostAt
                    )
                }else{
                    user.copy(
                        hearts = newHearts,
                        lastHeartLostAt = if (newHearts == 5) null else user.lastHeartLostAt
                    )
                }

            }
        }
    }

    suspend fun streakCheck(userId: String){
        val user = userDao.getUserById(userId)
        if (ChronoUnit.DAYS.between(user?.lastActiveDate, Instant.now())>=1){
            user?.streak =0
        }
        //Mozda refaktor ukoliko ponudim opciju korisniku da odgleda reklamu ili nesto slicno  kako bi produzio streak
    }

    suspend fun registerLessonActivity(userId: String){
        updateProgress(userId) { user ->
            val now = Instant.now()
            val today = now.atZone(ZoneOffset.UTC).toLocalDate()
            val lastActiveDay = user.lastActiveDate?.atZone(ZoneOffset.UTC)?.toLocalDate()

            when {
                lastActiveDay == null -> {
                    user.copy(
                        lastActiveDate = now,
                        streak = 1
                    )
                }
                lastActiveDay.isEqual(today) -> user
                lastActiveDay.plusDays(1).isEqual(today) -> {
                    user.copy(
                        lastActiveDate = now,
                        streak = user.streak + 1
                    )
                }
                else -> {
                    user.copy(
                        lastActiveDate = now,
                        streak = 1
                    )
                }
            }
        }
    }

    suspend fun ensureLocalUserProgress(userId: String, fallbackNickname: String? = null): Boolean {
        if (userId.isBlank()) return false
        val normalizedFallbackNickname = fallbackNickname?.trim().orEmpty()

        val cached = userDao.getUserById(userId)
        val hasCompleteCachedProfile = cached?.nickname?.isNotBlank() == true
        if (hasCompleteCachedProfile) return true

        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val remote = doc.toUserProgressEntity(userId)
                val merged = if (remote.nickname.isBlank() && normalizedFallbackNickname.isNotBlank()) {
                    remote.copy(nickname = normalizedFallbackNickname)
                } else {
                    remote
                }
                userDao.upsert(merged)
                true
            } else {
                val local = cached ?: createDefaultProgress(userId, normalizedFallbackNickname)
                val patchedLocal =
                    if (local.nickname.isBlank() && normalizedFallbackNickname.isNotBlank()) {
                        local.copy(
                            nickname = normalizedFallbackNickname,
                            updatedAt = Instant.now()
                        )
                    } else {
                        local
                    }
                userDao.upsert(patchedLocal)
                true
            }
        } catch (_: Exception) {
            val local = cached ?: createDefaultProgress(userId, normalizedFallbackNickname)
            val patchedLocal =
                if (local.nickname.isBlank() && normalizedFallbackNickname.isNotBlank()) {
                    local.copy(
                        nickname = normalizedFallbackNickname,
                        updatedAt = Instant.now()
                    )
                } else {
                    local
                }
            userDao.upsert(patchedLocal)
            true
        }
    }

    private fun createDefaultProgress(userId: String, nickname: String = ""): UserProgressEntity {
        return UserProgressEntity(
            userId = userId,
            nickname = nickname,
            xp = 0,
            level = 1,
            streak = 0,
            hearts = 5,
            lastHeartLostAt = null,
            lastActiveDate = Instant.now(),
            updatedAt = Instant.now()
        )
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
