package com.codeprep.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun askQuestion(@Body request: AiRequest): AiApiResponse
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
