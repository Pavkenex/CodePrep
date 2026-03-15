package com.codeprep.app.data.friends

internal const val DEFAULT_AVATAR_PRESET_ID = "avatar_01"

internal fun buildNicknameSearchTerms(nickname: String): List<String> {
    val normalized = nickname.trim().lowercase()
    if (normalized.length < 2) return emptyList()

    val terms = linkedSetOf<String>()
    for (start in normalized.indices) {
        for (end in (start + 2)..normalized.length) {
            terms += normalized.substring(start, end)
        }
    }
    return terms.toList()
}
