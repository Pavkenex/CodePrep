package com.example.codeprep.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.codeprep.data.local.entity.AiExplanationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiExplanationDao {
    @Query("SELECT * FROM ai_explanations WHERE question = :question AND (:lessonId IS NULL OR contextLessonId = :lessonId) AND cachedAt > :minTimestamp LIMIT 1")
    suspend fun getCachedAnswer(question: String, lessonId: String?, minTimestamp: Long): AiExplanationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheAnswer(entity: AiExplanationEntity)

    @Query("SELECT * FROM ai_explanations ORDER BY cachedAt DESC")
    fun getHistory(): Flow<List<AiExplanationEntity>>

    @Query("SELECT COUNT(*) FROM ai_explanations WHERE cachedAt > :since")
    suspend fun getQuestionCountSince(since: Long): Int
}