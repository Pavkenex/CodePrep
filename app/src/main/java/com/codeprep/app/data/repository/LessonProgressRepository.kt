package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.LessonProgressDao
import com.codeprep.app.data.local.entity.LessonProgressEntity
import com.codeprep.app.data.repository.LessonProgressRules.mergeWith
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject

class LessonProgressRepository @Inject constructor(
    private val lessonProgressDao: LessonProgressDao,
    private val firestore: FirebaseFirestore
) {
    fun observeProgressForUser(userId: String): Flow<List<LessonProgressEntity>> {
        return lessonProgressDao.observeByUser(userId)
    }

    suspend fun getLessonProgress(userId: String, lessonId: String): LessonProgressEntity? {
        return lessonProgressDao.getByUserAndLesson(userId, lessonId)
    }

    suspend fun saveAttempt(
        userId: String,
        lessonId: String,
        correctCount: Int,
        totalQuestions: Int,
        mistakeCount: Int,
        baseXp: Int
    ): SaveAttemptOutcome {
        val existing = lessonProgressDao.getByUserAndLesson(userId, lessonId)
        val evaluation = LessonProgressRules.evaluateAttempt(
            existing = existing,
            userId = userId,
            lessonId = lessonId,
            correctCount = correctCount,
            totalQuestions = totalQuestions,
            mistakeCount = mistakeCount,
            baseXp = baseXp,
            attemptedAt = Instant.now()
        )

        lessonProgressDao.upsert(evaluation.progress)

        try {
            pushProgressToRemote(evaluation.progress)
        } catch (_: Exception) {
            // Best-effort push. Session/periodic sync will retry later.
        }

        return SaveAttemptOutcome(
            progress = evaluation.progress,
            passed = evaluation.passed,
            perfect = evaluation.perfect,
            xpAwarded = evaluation.xpAwarded,
            awardedBaseXp = evaluation.awardedBaseXp,
            awardedPerfectBonus = evaluation.awardedPerfectBonus,
            perfectBonusXp = evaluation.perfectBonusXp
        )
    }

    suspend fun syncProgress(userId: String): SyncOutcome {
        if (userId.isBlank()) return SyncOutcome.NoOp

        val localProgress = lessonProgressDao.getByUser(userId)
        val remoteProgress = firestore.collection("users")
            .document(userId)
            .collection(LESSON_PROGRESS_COLLECTION)
            .get()
            .await()
            .documents
            .map { it.toLessonProgressEntity(userId) }

        val localByLesson = localProgress.associateBy { it.lessonId }
        val remoteByLesson = remoteProgress.associateBy { it.lessonId }
        val lessonIds = (localByLesson.keys + remoteByLesson.keys).distinct()

        val recordsToPull = mutableListOf<LessonProgressEntity>()
        val recordsToPush = mutableListOf<LessonProgressEntity>()

        lessonIds.forEach { lessonId ->
            val local = localByLesson[lessonId]
            val remote = remoteByLesson[lessonId]

            when {
                local == null && remote != null -> recordsToPull += remote
                local != null && remote == null -> recordsToPush += local
                local != null && remote != null -> {
                    val merged = local.mergeWith(remote)
                    if (merged != local) recordsToPull += merged
                    if (merged != remote) recordsToPush += merged
                }
            }
        }

        if (recordsToPull.isNotEmpty()) {
            lessonProgressDao.upsertAll(recordsToPull)
        }
        recordsToPush.forEach { pushProgressToRemote(it) }

        return when {
            recordsToPull.isNotEmpty() && recordsToPush.isNotEmpty() -> SyncOutcome.Merged
            recordsToPull.isNotEmpty() -> SyncOutcome.PulledRemote
            recordsToPush.isNotEmpty() -> SyncOutcome.PushedLocal
            else -> SyncOutcome.NoOp
        }
    }

    private suspend fun pushProgressToRemote(progress: LessonProgressEntity) {
        firestore.collection("users")
            .document(progress.userId)
            .collection(LESSON_PROGRESS_COLLECTION)
            .document(progress.lessonId)
            .set(progress.toRemoteMap(), SetOptions.merge())
            .await()
    }

    private fun LessonProgressEntity.toRemoteMap(): Map<String, Any?> {
        return mapOf(
            "completed" to completed,
            "perfectRun" to perfectRun,
            "bestCorrectCount" to bestCorrectCount,
            "totalQuestions" to totalQuestions,
            "mistakeCount" to mistakeCount,
            "completionXpAwarded" to completionXpAwarded,
            "perfectBonusAwarded" to perfectBonusAwarded,
            "lastAttemptAt" to lastAttemptAt
        )
    }

    private fun DocumentSnapshot.toLessonProgressEntity(userId: String): LessonProgressEntity {
        val bestCorrectCount = getLong("bestCorrectCount")?.toInt()
            ?: getLong("score")?.toInt()
            ?: 0
        return LessonProgressEntity(
            userId = userId,
            lessonId = id,
            completed = getBoolean("completed") ?: false,
            perfectRun = getBoolean("perfectRun") ?: false,
            bestCorrectCount = bestCorrectCount,
            totalQuestions = getLong("totalQuestions")?.toInt() ?: 0,
            mistakeCount = getLong("mistakeCount")?.toInt() ?: 0,
            completionXpAwarded = getBoolean("completionXpAwarded") ?: false,
            perfectBonusAwarded = getBoolean("perfectBonusAwarded") ?: false,
            lastAttemptAt = getTimestamp("lastAttemptAt")?.toInstant()
        )
    }

    data class SaveAttemptOutcome(
        val progress: LessonProgressEntity,
        val passed: Boolean,
        val perfect: Boolean,
        val xpAwarded: Int,
        val awardedBaseXp: Boolean,
        val awardedPerfectBonus: Boolean,
        val perfectBonusXp: Int
    )

    sealed interface SyncOutcome {
        data object NoOp : SyncOutcome
        data object PulledRemote : SyncOutcome
        data object PushedLocal : SyncOutcome
        data object Merged : SyncOutcome
    }

    companion object {
        private const val LESSON_PROGRESS_COLLECTION = "lessonProgress"

        fun calculatePerfectBonusXp(baseXp: Int): Int {
            return LessonProgressRules.calculatePerfectBonusXp(baseXp)
        }
    }
}
