package com.codeprep.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface OpenRouterApi {
    @POST
    suspend fun askQuestion(
        @Url url: String,
        @Body request: AiRequest,
        @Header("x-opencode-session") sessionId: String?,
        @Header("User-Agent") userAgent: String?
    ): AiApiResponse
}

data class AiRequest(
    val model: String,
    val messages: List<AiMessage>,
    val max_tokens: Int
)

data class AiMessage(
    val role: String,
    val content: String
)

data class AiApiResponse(
    val choices: List<AiChoice>
)

data class AiChoice(
    val message: AiMessage
)
