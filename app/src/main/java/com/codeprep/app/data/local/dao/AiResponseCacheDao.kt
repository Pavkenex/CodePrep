package com.codeprep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codeprep.app.data.local.entity.AiResponseCacheEntity

@Dao
interface AiResponseCacheDao {
    @Query(
        """
        SELECT * FROM ai_response_cache
        WHERE userId = :userId
        AND question = :question
        AND ((:lessonId IS NULL AND contextLessonId IS NULL) OR contextLessonId = :lessonId)
        AND cachedAt > :minTimestamp
        ORDER BY cachedAt DESC
        LIMIT 1
        """
    )
    suspend fun getCachedAnswer(
        userId: String,
        question: String,
        lessonId: String?,
        minTimestamp: Long
    ): AiResponseCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheAnswer(entity: AiResponseCacheEntity)

    @Query("SELECT COUNT(*) FROM ai_response_cache WHERE userId = :userId AND cachedAt > :since")
    suspend fun getQuestionCountSince(userId: String, since: Long): Int
}
