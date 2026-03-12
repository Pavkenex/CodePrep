package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.AiExplanationDao
import com.codeprep.app.data.local.entity.AiExplanationEntity
import com.codeprep.app.data.remote.api.AiConfig
import com.codeprep.app.data.remote.api.AiMessage
import com.codeprep.app.data.remote.api.AiRequest
import com.codeprep.app.data.remote.api.OpenRouterApi
import com.codeprep.app.domain.AiPromptBuilder
import com.codeprep.app.domain.model.AiResponse
import com.codeprep.app.domain.model.LessonContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val aiDao: AiExplanationDao,
    private val api: OpenRouterApi
) {
    suspend fun askQuestion(
        userId: String,
        question: String,
        context: LessonContext?
    ): AiResponse {
        if (userId.isBlank()) {
            return AiResponse.Error("Moraš biti ulogovan da bi koristio AI.")
        }

        val normalizedQuestion = question.trim()
        if (normalizedQuestion.isBlank()) {
            return AiResponse.Error("Pitanje ne može biti prazno.")
        }

        val now = System.currentTimeMillis()
        val minTimestamp = now - TimeUnit.DAYS.toMillis(AiConfig.CACHE_VALIDITY_DAYS)

        val freshCache = aiDao.getCachedAnswer(
            userId = userId,
            question = normalizedQuestion,
            lessonId = context?.lessonId,
            minTimestamp = minTimestamp
        )
        if (freshCache != null) {
            return AiResponse.Success(freshCache.answer, fromCache = true)
        }

        val zone = ZoneId.systemDefault()
        val todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val questionsToday = aiDao.getQuestionCountSince(userId, todayStart)
        if (questionsToday >= AiConfig.MAX_QUESTIONS_PER_DAY) {
            return AiResponse.RateLimited
        }

        val prompt = AiPromptBuilder.buildPrompt(normalizedQuestion, context)
        val maxTokens = if (context != null) {
            AiConfig.MAX_TOKENS_CONTEXTUAL
        } else {
            AiConfig.MAX_TOKENS_GLOBAL
        }

        return try {
            val response = api.askQuestion(
                AiRequest(
                    model = AiConfig.MODEL,
                    messages = listOf(AiMessage(role = "user", content = prompt)),
                    max_tokens = maxTokens
                )
            )

            val answer = response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
            if (answer.isBlank()) {
                resolveFallback(
                    userId = userId,
                    question = normalizedQuestion,
                    context = context,
                    defaultError = "AI nije vratio odgovor. Pokušaj ponovo."
                )
            } else {
                aiDao.cacheAnswer(
                    AiExplanationEntity(
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
            } else {
                resolveFallback(
                    userId = userId,
                    question = normalizedQuestion,
                    context = context,
                    defaultError = "AI servis trenutno nije dostupan (${e.code()})."
                )
            }
        } catch (_: IOException) {
            resolveFallback(
                userId = userId,
                question = normalizedQuestion,
                context = context,
                defaultError = "AI trenutno nije dostupan. Pokušaj ponovo."
            )
        }
    }

    fun getHistory(userId: String): Flow<List<AiExplanationEntity>> {
        if (userId.isBlank()) return flowOf(emptyList())
        return aiDao.getHistory(userId)
    }

    private suspend fun resolveFallback(
        userId: String,
        question: String,
        context: LessonContext?,
        defaultError: String
    ): AiResponse {
        val staleCache = aiDao.getCachedAnswer(
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
}
