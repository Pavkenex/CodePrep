package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.UserProgressEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.Duration
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
        val transformed = transform(current)
        if (transformed == current) return

        val updated = transformed.copy(
            updatedAt = Instant.now()
        )

        userDao.upsert(updated)
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
            if (user.hearts >= MAX_HEARTS || user.lastHeartLostAt == null) {
                user
            } else {
                val minutesPassed = ChronoUnit.MINUTES.between(user.lastHeartLostAt, Instant.now())
                val heartsToAdd = (minutesPassed / HEART_REFILL_MINUTES).toInt()
                val newHearts = (user.hearts + heartsToAdd).coerceAtMost(MAX_HEARTS)

                if (newHearts < MAX_HEARTS) {
                    val updatedLastHeartLostAt = user.lastHeartLostAt.plus(
                        heartsToAdd * HEART_REFILL_MINUTES.toLong(),
                        ChronoUnit.MINUTES
                    )
                    user.copy(
                        hearts = newHearts,
                        lastHeartLostAt = updatedLastHeartLostAt
                    )
                } else {
                    user.copy(
                        hearts = newHearts,
                        lastHeartLostAt = null
                    )
                }
            }
        }
    }

    suspend fun streakCheck(userId: String) {
        updateProgress(userId) { user ->
            val lastActiveDay = user.lastActiveDate?.atZone(ZoneOffset.UTC)?.toLocalDate()
                ?: return@updateProgress user
            val today = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()

            if (lastActiveDay.plusDays(1).isBefore(today) && user.streak != 0) {
                user.copy(streak = 0)
            } else {
                user
            }
        }
        // Mozda refaktor ukoliko ponudim opciju korisniku da odgleda reklamu ili nesto slicno kako bi produzio streak
    }

    suspend fun registerStreakActivity(userId: String) {
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

    suspend fun syncProgress(userId: String): SyncOutcome {
        if (userId.isBlank()) return SyncOutcome.NoOp

        val local = userDao.getUserById(userId)
        val remoteSnapshot = firestore.collection("users").document(userId).get().await()
        val remote = remoteSnapshot.takeIf { it.exists() }?.toUserProgressEntity(userId)

        return when {
            local == null && remote == null -> SyncOutcome.NoOp
            local != null && remote == null -> {
                pushProgressToRemote(local)
                SyncOutcome.PushedLocal
            }
            local == null && remote != null -> {
                userDao.upsert(remote)
                SyncOutcome.PulledRemote
            }
            local != null && remote != null -> {
                val localUpdatedAt = local.updatedAt ?: Instant.EPOCH
                val remoteUpdatedAt = remote.updatedAt ?: Instant.EPOCH

                when {
                    localUpdatedAt.isAfter(remoteUpdatedAt) -> {
                        pushProgressToRemote(local)
                        SyncOutcome.PushedLocal
                    }
                    remoteUpdatedAt.isAfter(localUpdatedAt) -> {
                        userDao.upsert(remote)
                        SyncOutcome.PulledRemote
                    }
                    else -> SyncOutcome.NoOp
                }
            }
            else -> SyncOutcome.NoOp
        }
    }

    fun calculateTimeUntilFullHearts(progress: UserProgressEntity, now: Instant = Instant.now()): Duration? {
        if (progress.hearts >= MAX_HEARTS || progress.lastHeartLostAt == null) return null

        val missingHearts = (MAX_HEARTS - progress.hearts).coerceAtLeast(0)
        val fullRefillAt = progress.lastHeartLostAt.plus(
            (missingHearts * HEART_REFILL_MINUTES).toLong(),
            ChronoUnit.MINUTES
        )

        return Duration.between(now, fullRefillAt)
    }

    fun calculateTimeUntilNextHeart(progress: UserProgressEntity, now: Instant = Instant.now()): Duration? {
        return calculateTimeUntilNextHeartAt(progress, now)
    }

    fun calculateStreakResetTime(progress: UserProgressEntity): Instant? {
        val lastActiveDay = progress.lastActiveDate?.atZone(ZoneOffset.UTC)?.toLocalDate() ?: return null
        return lastActiveDay
            .plusDays(2)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
    }

    private suspend fun pushProgressToRemote(progress: UserProgressEntity) {
        firestore.collection("users")
            .document(progress.userId)
            .set(progress, SetOptions.merge())
            .await()
    }

    private fun createDefaultProgress(userId: String, nickname: String = ""): UserProgressEntity {
        return UserProgressEntity(
            userId = userId,
            nickname = nickname,
            xp = 0,
            level = 1,
            streak = 0,
            hearts = MAX_HEARTS,
            lastHeartLostAt = null,
            lastActiveDate = null,
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
            hearts = getLong("hearts")?.toInt() ?: MAX_HEARTS,
            lastHeartLostAt = getTimestamp("lastHeartLostAt")?.toInstant(),
            lastActiveDate = getTimestamp("lastActiveDate")?.toInstant(),
            updatedAt = getTimestamp("updatedAt")?.toInstant() ?: Instant.now()
        )
    }

    sealed interface SyncOutcome {
        data object NoOp : SyncOutcome
        data object PulledRemote : SyncOutcome
        data object PushedLocal : SyncOutcome
    }

    companion object {
        const val MAX_HEARTS = 5
        const val HEART_REFILL_MINUTES = 30

        fun calculateTimeUntilNextHeartAt(
            progress: UserProgressEntity,
            now: Instant = Instant.now()
        ): Duration? {
            if (progress.hearts >= MAX_HEARTS || progress.lastHeartLostAt == null) return null

            val nextRefillAt = progress.lastHeartLostAt.plus(
                HEART_REFILL_MINUTES.toLong(),
                ChronoUnit.MINUTES
            )
            val remaining = Duration.between(now, nextRefillAt)
            return if (remaining.isNegative) Duration.ZERO else remaining
        }
    }
}
