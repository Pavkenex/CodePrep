package com.codeprep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.codeprep.app.data.local.entity.AiConversationEntity
import com.codeprep.app.data.local.entity.AiConversationMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: AiConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<AiConversationMessageEntity>)

    @Query("DELETE FROM ai_conversation_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query(
        """
        DELETE FROM ai_conversations
        WHERE userId = :userId AND lessonId = :lessonId
        """
    )
    suspend fun deleteConversationForLesson(
        userId: String,
        lessonId: String
    )

    @Query(
        """
        SELECT * FROM ai_conversations
        WHERE userId = :userId AND lessonId = :lessonId
        LIMIT 1
        """
    )
    suspend fun getConversationForLesson(
        userId: String,
        lessonId: String
    ): AiConversationEntity?

    @Query(
        """
        SELECT * FROM ai_conversation_messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
        """
    )
    suspend fun getMessagesForConversation(conversationId: String): List<AiConversationMessageEntity>

    @Query(
        """
        SELECT * FROM ai_conversations
        WHERE userId = :userId
        ORDER BY updatedAt DESC
        """
    )
    suspend fun getAllConversations(userId: String): List<AiConversationEntity>

    @Query(
        """
        SELECT * FROM ai_conversation_messages
        WHERE conversationId IN (:conversationIds)
        ORDER BY createdAt ASC
        """
    )
    suspend fun getMessagesForConversations(conversationIds: List<String>): List<AiConversationMessageEntity>

    @Query(
        """
        SELECT c.id,
               c.lessonId,
               c.courseTitle,
               c.lessonTitle,
               c.createdAt,
               c.updatedAt,
               COUNT(m.id) AS messageCount,
               (
                   SELECT content
                   FROM ai_conversation_messages latest
                   WHERE latest.conversationId = c.id
                   ORDER BY latest.createdAt DESC
                   LIMIT 1
               ) AS preview
        FROM ai_conversations c
        LEFT JOIN ai_conversation_messages m ON m.conversationId = c.id
        WHERE c.userId = :userId
        GROUP BY c.id
        ORDER BY c.updatedAt DESC
        """
    )
    fun observeConversationSummaries(userId: String): Flow<List<AiConversationSummaryRow>>

    @Transaction
    suspend fun getConversationWithMessages(
        userId: String,
        lessonId: String
    ): AiConversationWithMessages? {
        val conversation = getConversationForLesson(userId, lessonId) ?: return null
        return AiConversationWithMessages(
            conversation = conversation,
            messages = getMessagesForConversation(conversation.id)
        )
    }

    @Transaction
    suspend fun getAllConversationsWithMessages(userId: String): List<AiConversationWithMessages> {
        val conversations = getAllConversations(userId)
        if (conversations.isEmpty()) {
            return emptyList()
        }

        val messagesByConversationId = getMessagesForConversations(conversations.map { it.id })
            .groupBy { it.conversationId }

        return conversations.map { conversation ->
            AiConversationWithMessages(
                conversation = conversation,
                messages = messagesByConversationId[conversation.id].orEmpty()
            )
        }
    }
}

data class AiConversationWithMessages(
    val conversation: AiConversationEntity,
    val messages: List<AiConversationMessageEntity>
)

data class AiConversationSummaryRow(
    val id: String,
    val lessonId: String,
    val courseTitle: String,
    val lessonTitle: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messageCount: Int,
    val preview: String?
)
