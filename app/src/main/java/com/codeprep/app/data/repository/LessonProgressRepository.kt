package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.LessonProgressDao
import com.codeprep.app.data.local.entity.LessonProgressEntity
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
        score: Int,
        totalQuestions: Int,
        mistakeCount: Int
    ) {
        val attempt = LessonProgressEntity(
            userId = userId,
            lessonId = lessonId,
            completed = totalQuestions > 0,
            perfectRun = totalQuestions > 0 && mistakeCount == 0,
            score = score,
            mistakeCount = mistakeCount,
            lastAttemptAt = Instant.now()
        )

        lessonProgressDao.upsert(attempt)

        try {
            pushProgressToRemote(attempt)
        } catch (_: Exception) {
            // Best-effort push. Session/periodic sync will retry later.
        }
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
                    val localAttemptAt = local.lastAttemptAt ?: Instant.EPOCH
                    val remoteAttemptAt = remote.lastAttemptAt ?: Instant.EPOCH

                    when {
                        remoteAttemptAt.isAfter(localAttemptAt) -> recordsToPull += remote
                        localAttemptAt.isAfter(remoteAttemptAt) -> recordsToPush += local
                    }
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
            "score" to score,
            "mistakeCount" to mistakeCount,
            "lastAttemptAt" to lastAttemptAt
        )
    }

    private fun DocumentSnapshot.toLessonProgressEntity(userId: String): LessonProgressEntity {
        return LessonProgressEntity(
            userId = userId,
            lessonId = id,
            completed = getBoolean("completed") ?: false,
            perfectRun = getBoolean("perfectRun") ?: false,
            score = getLong("score")?.toInt() ?: 0,
            mistakeCount = getLong("mistakeCount")?.toInt() ?: 0,
            lastAttemptAt = getTimestamp("lastAttemptAt")?.toInstant()
        )
    }

    sealed interface SyncOutcome {
        data object NoOp : SyncOutcome
        data object PulledRemote : SyncOutcome
        data object PushedLocal : SyncOutcome
        data object Merged : SyncOutcome
    }

    companion object {
        private const val LESSON_PROGRESS_COLLECTION = "lessonProgress"
    }
}
