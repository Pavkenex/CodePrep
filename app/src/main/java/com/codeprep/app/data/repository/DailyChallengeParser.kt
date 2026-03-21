package com.codeprep.app.data.repository

import com.codeprep.app.data.local.entity.Question

internal fun parseDailyChallengeQuestion(
    data: Map<String, Any?>,
    selectedLanguage: String,
    fallbackId: String
): Question? {
    val prompt = data.localizedString("prompt", selectedLanguage)
        ?: data.localizedString("promptLocalized", selectedLanguage)
        ?: return null

    val options = parseDailyChallengeOptions(
        rawOptions = data["options"],
        selectedLanguage = selectedLanguage
    )
    if (options.isEmpty()) return null

    val correctIndex = resolveDailyChallengeCorrectIndex(
        data = data,
        options = options
    )

    val codeSnippet = parseDailyChallengeCodeSnippet(data["codeSnippet"])

    return Question(
        id = data.string("id") ?: fallbackId,
        title = data.localizedString("title", selectedLanguage).orEmpty(),
        moduleId = "",
        lessonId = "",
        lessonRefPath = data.string("locale").orEmpty(),
        text = prompt,
        options = options.map { it.text },
        correctIndex = correctIndex,
        explanation = data.localizedString("explanation", selectedLanguage)
            ?: data.localizedString("description", selectedLanguage)
            ?: "",
        type = data.string("challengeType").orEmpty(),
        difficulty = data.string("difficulty").orEmpty(),
        difficultyWeight = data.number("pointsReward") ?: 0.0,
        tags = data.stringList("tags"),
        format = data.string("format") ?: "single_choice",
        quizEnabled = data.boolean("quizEnabled") ?: true,
        randomKey = 0L,
        orderIndex = 0,
        seedVersion = data.stringOrNumber("version").orEmpty(),
        codeSnippetLanguage = codeSnippet?.language,
        codeSnippet = codeSnippet?.code
    )
}

private data class ParsedDailyChallengeOption(
    val id: String?,
    val text: String
)

private data class ParsedDailyChallengeCodeSnippet(
    val language: String?,
    val code: String
)

private fun parseDailyChallengeOptions(
    rawOptions: Any?,
    selectedLanguage: String
): List<ParsedDailyChallengeOption> {
    val options = rawOptions as? List<*> ?: return emptyList()
    return options.mapIndexedNotNull { index, option ->
        when (option) {
            is String -> option.takeIf { it.isNotBlank() }?.let {
                ParsedDailyChallengeOption(id = index.toString(), text = it)
            }

            is Map<*, *> -> {
                val text = option.localizedValueFromAnyMap("text", selectedLanguage)
                    ?: option.stringValue("text")
                    ?: return@mapIndexedNotNull null
                ParsedDailyChallengeOption(
                    id = option.stringValue("id"),
                    text = text
                )
            }

            else -> null
        }
    }
}

private fun resolveDailyChallengeCorrectIndex(
    data: Map<String, Any?>,
    options: List<ParsedDailyChallengeOption>
): Int {
    val correctOptionId = data.string("correctOptionId")
    if (!correctOptionId.isNullOrBlank()) {
        val matchedIndex = options.indexOfFirst { it.id == correctOptionId }
        if (matchedIndex >= 0) return matchedIndex
    }

    val legacyIndexes = (data["correctOptionIndexes"] as? List<*>)?.mapNotNull { raw ->
        when (raw) {
            is Int -> raw
            is Long -> raw.toInt()
            is Double -> raw.toInt()
            else -> null
        }
    }.orEmpty()

    return legacyIndexes.firstOrNull()?.coerceIn(0, options.lastIndex) ?: 0
}

private fun parseDailyChallengeCodeSnippet(rawValue: Any?): ParsedDailyChallengeCodeSnippet? {
    return when (rawValue) {
        is String -> rawValue.takeIf { it.isNotBlank() }?.let {
            ParsedDailyChallengeCodeSnippet(language = null, code = it)
        }

        is Map<*, *> -> {
            val code = rawValue.stringValue("code")?.takeIf { it.isNotBlank() } ?: return null
            ParsedDailyChallengeCodeSnippet(
                language = rawValue.stringValue("language"),
                code = code
            )
        }

        else -> null
    }
}

private fun Map<String, Any?>.string(key: String): String? = when (val value = this[key]) {
    is String -> value.takeIf { it.isNotBlank() }
    else -> null
}

private fun Map<String, Any?>.number(key: String): Double? = when (val value = this[key]) {
    is Double -> value
    is Float -> value.toDouble()
    is Long -> value.toDouble()
    is Int -> value.toDouble()
    else -> null
}

private fun Map<String, Any?>.stringOrNumber(key: String): String? = when (val value = this[key]) {
    is String -> value.takeIf { it.isNotBlank() }
    is Long -> value.toString()
    is Int -> value.toString()
    is Double -> value.toString()
    is Float -> value.toString()
    else -> null
}

private fun Map<String, Any?>.boolean(key: String): Boolean? = this[key] as? Boolean

private fun Map<String, Any?>.stringList(key: String): List<String> {
    return (this[key] as? List<*>)?.mapNotNull { it as? String }?.filter { it.isNotBlank() }.orEmpty()
}

private fun Map<String, Any?>.localizedString(key: String, selectedLanguage: String): String? {
    return localizedValueFromTypedMap(key, selectedLanguage) ?: string(key)
}

private fun Map<String, Any?>.localizedValueFromTypedMap(
    key: String,
    selectedLanguage: String
): String? {
    val localizedMap = this[key] as? Map<*, *> ?: return null
    val preferred = localizedMap[selectedLanguage] as? String
    val english = localizedMap["en"] as? String
    val serbian = localizedMap["sr"] as? String
    return preferred?.takeIf { it.isNotBlank() }
        ?: english?.takeIf { it.isNotBlank() }
        ?: serbian?.takeIf { it.isNotBlank() }
}

private fun Map<*, *>.stringValue(key: String): String? = when (val value = this[key]) {
    is String -> value.takeIf { it.isNotBlank() }
    else -> null
}

private fun Map<*, *>.localizedValueFromAnyMap(key: String, selectedLanguage: String): String? {
    val localizedMap = this[key] as? Map<*, *> ?: return null
    val preferred = localizedMap[selectedLanguage] as? String
    val english = localizedMap["en"] as? String
    val serbian = localizedMap["sr"] as? String
    return preferred?.takeIf { it.isNotBlank() }
        ?: english?.takeIf { it.isNotBlank() }
        ?: serbian?.takeIf { it.isNotBlank() }
}
