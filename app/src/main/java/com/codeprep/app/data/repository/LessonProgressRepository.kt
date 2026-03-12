package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.LessonProgressDao
import com.codeprep.app.data.local.entity.LessonProgressEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class LessonProgressRepository @Inject constructor(
    private val lessonProgressDao: LessonProgressDao
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
        lessonProgressDao.upsert(
            LessonProgressEntity(
                userId = userId,
                lessonId = lessonId,
                completed = totalQuestions > 0,
                perfectRun = totalQuestions > 0 && mistakeCount == 0,
                score = score,
                mistakeCount = mistakeCount,
                lastAttemptAt = Instant.now()
            )
        )
    }
}

