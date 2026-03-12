package com.codeprep.app.domain.model

sealed class AiResponse {
    data class Success(val answer: String, val fromCache: Boolean) : AiResponse()
    data class Fallback(val summary: String) : AiResponse()
    data class Error(val message: String) : AiResponse()
    object RateLimited : AiResponse()
}
