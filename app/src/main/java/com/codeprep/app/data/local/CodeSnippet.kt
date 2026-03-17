package com.codeprep.app.data.local

import com.codeprep.app.data.model.LocalizedText

data class CodeSnippet(
    val language: String,
    val description: LocalizedText,
    val code: String,
    val isAntiPattern: Boolean
)
