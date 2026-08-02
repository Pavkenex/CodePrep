package com.codeprep.app.data.repository

import com.codeprep.app.R
import com.codeprep.app.data.backup.ImportedAiConversationBackup
import com.codeprep.app.data.backup.deserializeAiConversationBackup
import com.codeprep.app.data.backup.serializeAiConversationBackup
import com.codeprep.app.data.local.dao.AiConversationDao
import com.codeprep.app.data.local.dao.AiConversationWithMessages
import com.codeprep.app.data.local.dao.AiResponseCacheDao
import com.codeprep.app.data.local.entity.AiConversationEntity
import com.codeprep.app.data.local.entity.AiConversationMessageEntity
import com.codeprep.app.data.local.entity.AiResponseCacheEntity
import com.codeprep.app.data.remote.api.AiConfig
import com.codeprep.app.data.remote.api.AiMessage
import com.codeprep.app.data.remote.api.AiRequest
import com.codeprep.app.data.remote.api.OpenRouterApi
import com.codeprep.app.data.settings.AppStringProvider
import com.codeprep.app.data.settings.AiSettingsStore
import com.codeprep.app.domain.AiPromptBuilder
import com.codeprep.app.domain.model.AiConversationMessage
import com.codeprep.app.domain.model.AiConversationRole
import com.codeprep.app.domain.model.AiResponse
import com.codeprep.app.domain.model.LessonContext
import com.codeprep.app.domain.model.SavedAiConversation
import com.codeprep.app.domain.model.SavedAiConversationSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val conversationDao: AiConversationDao,
    private val cacheDao: AiResponseCacheDao,
    private val strings: AppStringProvider,
    private val aiSettingsStore: AiSettingsStore,
    private val api: OpenRouterApi
) {
    suspend fun askQuestion(
        userId: String,
        question: String,
        context: LessonContext?,
        conversationHistory: List<AiConversationMessage>
    ): AiResponse {
        if (userId.isBlank()) {
            return AiResponse.Error(strings.get(R.string.ai_login_required))
        }

        val normalizedQuestion = question.trim()
        if (normalizedQuestion.isBlank()) {
            return AiResponse.Error(strings.get(R.string.ai_question_empty))
        }

        val now = System.currentTimeMillis()
        val minTimestamp = now - TimeUnit.DAYS.toMillis(AiConfig.CACHE_VALIDITY_DAYS)

        val canUseCache = conversationHistory.isEmpty()
        if (canUseCache) {
            // NOTE: cached answers are valid for AiConfig.CACHE_VALIDITY_DAYS and are
            // keyed by userId/question/lessonId, not by provider settings. After the
            // user switches AI providers (base URL/model/key), a fresh question that
            // matches a cached one may still be served from cache. Accepted behavior.
            val freshCache = cacheDao.getCachedAnswer(
                userId = userId,
                question = normalizedQuestion,
                lessonId = context?.lessonId,
                minTimestamp = minTimestamp
            )
            if (freshCache != null) {
                return AiResponse.Success(freshCache.answer, fromCache = true)
            }
        }

        val zone = ZoneId.systemDefault()
        val todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val questionsToday = cacheDao.getQuestionCountSince(userId, todayStart)
        if (questionsToday >= AiConfig.MAX_QUESTIONS_PER_DAY) {
            return AiResponse.RateLimited
        }

        val systemPrompt = AiPromptBuilder.buildSystemPrompt(context)
        val maxTokens = if (context != null) {
            AiConfig.MAX_TOKENS_CONTEXTUAL
        } else {
            AiConfig.MAX_TOKENS_GLOBAL
        }

        return try {
            val response = api.askQuestion(
                AiRequest(
                    model = aiSettingsStore.getModelId().trim(),
                    messages = buildApiMessages(
                        systemPrompt = systemPrompt,
                        conversationHistory = conversationHistory,
                        question = normalizedQuestion
                    ),
                    max_tokens = maxTokens
                )
            )

            val answer = response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
            if (answer.isBlank()) {
                resolveFallback(
                    userId = userId,
                    question = normalizedQuestion,
                    context = context,
                    defaultError = strings.get(R.string.ai_no_answer)
                )
            } else {
                cacheDao.cacheAnswer(
                    AiResponseCacheEntity(
                        userId = userId,
                        question = normalizedQuestion,
                        contextLessonId = context?.lessonId,
                        answer = answer,
                        cachedAt = now
                    )
                )
                AiResponse.Success(answer, fromCache = false)
            }
        } catch (e: HttpException) {
            if (e.code() == 429) {
                AiResponse.RateLimited
            } else if (e.code() == 401) {
                // The configured API key was rejected by the provider.
                resolveFallback(
                    userId = userId,
                    question = normalizedQuestion,
                    context = context,
                    defaultError = strings.get(R.string.ai_invalid_api_key)
                )
            } else {
                // 403 (and every other status) intentionally falls through to the
                // generic service-unavailable message; the daily-limit 429 case is
                // handled above and the provider does not report a distinct
                // meaning for 403 that warrants a dedicated message.
                resolveFallback(
                    userId = userId,
                    question = normalizedQuestion,
                    context = context,
                    defaultError = strings.get(R.string.ai_service_unavailable, e.code())
                )
            }
        } catch (_: IOException) {
            resolveFallback(
                userId = userId,
                question = normalizedQuestion,
                context = context,
                defaultError = strings.get(R.string.ai_unavailable_try_again)
            )
        } catch (_: IllegalArgumentException) {
            // Malformed user-configured base URL: the interceptor cannot build a
            // request URL. Degrade to the same retry message instead of crashing.
            resolveFallback(
                userId = userId,
                question = normalizedQuestion,
                context = context,
                defaultError = strings.get(R.string.ai_unavailable_try_again)
            )
        }
    }

    fun getSavedConversations(userId: String): Flow<List<SavedAiConversationSummary>> {
        if (userId.isBlank()) return flowOf(emptyList())
        return conversationDao.observeConversationSummaries(userId).map { rows ->
            rows.map { row ->
                SavedAiConversationSummary(
                    id = row.id,
                    lessonId = row.lessonId,
                    courseTitle = row.courseTitle,
                    lessonTitle = row.lessonTitle,
                    preview = row.preview.orEmpty(),
                    messageCount = row.messageCount,
                    updatedAt = row.updatedAt
                )
            }
        }
    }

    suspend fun getSavedConversation(
        userId: String,
        lessonId: String
    ): SavedAiConversation? {
        if (userId.isBlank() || lessonId.isBlank()) return null
        return conversationDao.getConversationWithMessages(userId, lessonId)?.toDomain()
    }

    suspend fun deleteSavedConversation(
        userId: String,
        lessonId: String
    ) {
        if (userId.isBlank() || lessonId.isBlank()) return
        conversationDao.deleteConversationForLesson(userId, lessonId)
    }

    suspend fun exportSavedConversations(userId: String): String {
        if (userId.isBlank()) {
            return serializeAiConversationBackup(emptyList())
        }

        return serializeAiConversationBackup(
            conversationDao.getAllConversationsWithMessages(userId).map { it.toDomain() }
        )
    }

    suspend fun importSavedConversations(
        userId: String,
        payload: String
    ): Int {
        if (userId.isBlank() || payload.isBlank()) return 0

        val conversations = deserializeAiConversationBackup(payload)
        conversations.forEach { persistImportedConversation(userId, it) }
        return conversations.size
    }

    suspend fun saveConversation(
        userId: String,
        context: LessonContext,
        messages: List<AiConversationMessage>
    ): SavedAiConversation {
        val now = System.currentTimeMillis()
        val existing = conversationDao.getConversationForLesson(userId, context.lessonId)
        val conversationId = existing?.id ?: UUID.randomUUID().toString()
        val conversation = AiConversationEntity(
            id = conversationId,
            userId = userId,
            lessonId = context.lessonId,
            courseTitle = context.courseTitle,
            lessonTitle = context.lessonTitle,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        val persistedMessages = messages
            .filter { it.role != AiConversationRole.System && it.content.isNotBlank() }
            .map { message ->
                AiConversationMessageEntity(
                    id = message.id,
                    conversationId = conversationId,
                    role = message.role.toStorageRole(),
                    content = message.content,
                    createdAt = message.createdAt
                )
            }

        persistConversation(conversation, persistedMessages)

        return SavedAiConversation(
            id = conversation.id,
            lessonId = conversation.lessonId,
            courseTitle = conversation.courseTitle,
            lessonTitle = conversation.lessonTitle,
            createdAt = conversation.createdAt,
            updatedAt = conversation.updatedAt,
            messages = persistedMessages.map { it.toDomain() }
        )
    }

    private suspend fun persistImportedConversation(
        userId: String,
        conversation: ImportedAiConversationBackup
    ) {
        val now = System.currentTimeMillis()
        val existing = conversationDao.getConversationForLesson(userId, conversation.lessonId)
        val conversationId = existing?.id ?: UUID.randomUUID().toString()
        val entity = AiConversationEntity(
            id = conversationId,
            userId = userId,
            lessonId = conversation.lessonId,
            courseTitle = conversation.courseTitle,
            lessonTitle = conversation.lessonTitle,
            createdAt = conversation.createdAt.takeIf { it > 0L } ?: existing?.createdAt ?: now,
            updatedAt = conversation.updatedAt.takeIf { it > 0L } ?: now
        )
        val messages = conversation.messages
            .filter { it.content.isNotBlank() }
            .mapIndexed { index, message ->
                AiConversationMessageEntity(
                    id = message.id.ifBlank { "imported-${conversation.lessonId}-${message.createdAt}-$index" },
                    conversationId = conversationId,
                    role = message.role.toStorageRole(),
                    content = message.content,
                    createdAt = message.createdAt
                )
            }

        persistConversation(entity, messages)
    }

    private suspend fun persistConversation(
        conversation: AiConversationEntity,
        messages: List<AiConversationMessageEntity>
    ) {
        conversationDao.upsertConversation(conversation)
        conversationDao.deleteMessagesForConversation(conversation.id)
        if (messages.isNotEmpty()) {
            conversationDao.insertMessages(messages)
        }
    }

    private suspend fun resolveFallback(
        userId: String,
        question: String,
        context: LessonContext?,
        defaultError: String
    ): AiResponse {
        val staleCache = cacheDao.getCachedAnswer(
            userId = userId,
            question = question,
            lessonId = context?.lessonId,
            minTimestamp = 0L
        )

        return when {
            staleCache != null -> AiResponse.Success(staleCache.answer, fromCache = true)
            context != null && context.theorySummary.isNotBlank() -> AiResponse.Fallback(context.theorySummary)
            else -> AiResponse.Error(defaultError)
        }
    }

    private fun buildApiMessages(
        systemPrompt: String,
        conversationHistory: List<AiConversationMessage>,
        question: String
    ): List<AiMessage> {
        return buildList {
            add(AiMessage(role = "system", content = systemPrompt))
            conversationHistory.forEach { message ->
                val role = when (message.role) {
                    AiConversationRole.User -> "user"
                    AiConversationRole.Assistant -> "assistant"
                    AiConversationRole.System -> null
                }
                if (role != null) {
                    add(AiMessage(role = role, content = message.content))
                }
            }
            add(AiMessage(role = "user", content = question))
        }
    }

    private fun AiConversationWithMessages.toDomain(): SavedAiConversation {
        return SavedAiConversation(
            id = conversation.id,
            lessonId = conversation.lessonId,
            courseTitle = conversation.courseTitle,
            lessonTitle = conversation.lessonTitle,
            createdAt = conversation.createdAt,
            updatedAt = conversation.updatedAt,
            messages = messages.map { it.toDomain() }
        )
    }

    private fun AiConversationMessageEntity.toDomain(): AiConversationMessage {
        return AiConversationMessage(
            id = id,
            role = role.toConversationRole(),
            content = content,
            createdAt = createdAt
        )
    }

    private fun String.toConversationRole(): AiConversationRole {
        return when (this) {
            "assistant" -> AiConversationRole.Assistant
            "system" -> AiConversationRole.System
            else -> AiConversationRole.User
        }
    }

    private fun AiConversationRole.toStorageRole(): String {
        return when (this) {
            AiConversationRole.User -> "user"
            AiConversationRole.Assistant -> "assistant"
            AiConversationRole.System -> "system"
        }
    }
}
