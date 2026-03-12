package com.codeprep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.codeprep.app.data.local.entity.LessonProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonProgressDao {
    @Query("SELECT * FROM lesson_progress WHERE userId = :userId")
    fun observeByUser(userId: String): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    suspend fun getByUserAndLesson(userId: String, lessonId: String): LessonProgressEntity?

    @Upsert
    suspend fun upsert(progress: LessonProgressEntity)
}

