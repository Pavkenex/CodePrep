package com.codeprep.app.data.model

import java.util.Locale

data class LocalizedText(
    val en: String = "",
    val sr: String = ""
) {
    fun resolve(languageCode: String? = null): String {
        val normalizedLanguage = languageCode
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }
            ?: Locale.getDefault().language.lowercase(Locale.ROOT)

        val selected = when {
            normalizedLanguage.startsWith("sr") -> sr
            normalizedLanguage.startsWith("en") -> en
            else -> ""
        }.trim()

        if (selected.isNotEmpty()) return selected

        val english = en.trim()
        if (english.isNotEmpty()) return english

        val serbian = sr.trim()
        if (serbian.isNotEmpty()) return serbian

        return ""
    }
}

data class LessonContentBlock(
    val introduction: LocalizedText = LocalizedText(),
    val explanation: LocalizedText = LocalizedText(),
    val example: LocalizedText = LocalizedText(),
    val keyTakeaway: LocalizedText = LocalizedText()
)
