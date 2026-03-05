package com.codeprep.app.data.local

data class CodeSnippet(
    val language: String,
    val description: String,
    val code: String,
    val isAntiPattern: Boolean
)