package com.codeprep.app.data.model

import com.codeprep.app.data.settings.AppSettingsStore
import java.time.LocalDate

data class FunFact(
    val id: String,
    val content: LocalizedText,
    val categoryKey: String = SecretFactStrings.DEFAULT_CATEGORY_KEY
)

data class DailyFunFact(
    val id: String,
    val text: String,
    val languageCode: String,
    val epochDay: Long,
    val categoryKey: String = SecretFactStrings.DEFAULT_CATEGORY_KEY
)

data class FunFactCacheSnapshot(
    val id: String,
    val text: String,
    val languageCode: String,
    val epochDay: Long
)

data class FunFactWidgetUiState(
    val badge: String,
    val factText: String,
    val footer: String,
    val isLoggedIn: Boolean
)

object FunFactRotation {
    fun sortFacts(facts: List<FunFact>): List<FunFact> {
        return facts.sortedWith(
            compareBy<FunFact>({ extractNumericSuffix(it.id) }, { it.id })
        )
    }

    fun indexForDate(date: LocalDate, factCount: Int): Int {
        require(factCount > 0) { "factCount must be greater than 0" }
        return Math.floorMod(date.toEpochDay(), factCount.toLong()).toInt()
    }

    fun randomIndex(date: LocalDate, factCount: Int, salt: Long): Int {
        require(factCount > 0) { "factCount must be greater than 0" }
        return Math.floorMod(date.toEpochDay() + salt, factCount.toLong()).toInt()
    }

    internal fun extractNumericSuffix(id: String): Int {
        val digits = id.filter(Char::isDigit)
        return digits.toIntOrNull() ?: Int.MAX_VALUE
    }
}

object FunFactStrings {
    fun normalizedLanguage(languageCode: String?): String {
        val normalized = languageCode?.trim()?.lowercase().orEmpty()
        return when {
            normalized.startsWith("sr") -> "sr"
            else -> AppSettingsStore.DEFAULT_LANGUAGE
        }
    }

    fun badge(languageCode: String): String {
        return when (normalizedLanguage(languageCode)) {
            "sr" -> "Dnevna zanimljivost"
            else -> "Daily fun fact"
        }
    }

    fun footer(languageCode: String): String {
        return when (normalizedLanguage(languageCode)) {
            "sr" -> "Tapni za CodePrep"
            else -> "Tap to open CodePrep"
        }
    }

    fun tapHint(languageCode: String): String {
        return when (normalizedLanguage(languageCode)) {
            "sr" -> "Tapni bilo gde ili sacekaj trenutak"
            else -> "Tap anywhere or wait a moment"
        }
    }

    fun openButton(languageCode: String): String {
        return when (normalizedLanguage(languageCode)) {
            "sr" -> "Otvori CodePrep"
            else -> "Open CodePrep"
        }
    }

    fun neutralMessage(languageCode: String): String {
        return when (normalizedLanguage(languageCode)) {
            "sr" -> "Uloguj se da vidis danasnju zanimljivost"
            else -> "Log in to see today's fact"
        }
    }

    fun fallbackMessage(languageCode: String): String {
        return when (normalizedLanguage(languageCode)) {
            "sr" -> "Otvori CodePrep da ucitas danasnju zanimljivost"
            else -> "Open CodePrep to load today's fact"
        }
    }

    fun splashTitle(languageCode: String): String {
        return when (normalizedLanguage(languageCode)) {
            "sr" -> "Zanimljivost dana"
            else -> "Today's fun fact"
        }
    }
}

object SecretFactStrings {
    const val DEFAULT_CATEGORY_KEY = "programming"

    fun armedHint(languageCode: String): String {
        return when (FunFactStrings.normalizedLanguage(languageCode)) {
            "sr" -> "Tajna zanimljivost je spremna..."
            else -> "Secret fact ready..."
        }
    }

    fun revealTitle(languageCode: String): String {
        return when (FunFactStrings.normalizedLanguage(languageCode)) {
            "sr" -> "Otkljucana tajna zanimljivost"
            else -> "Secret Fact Unlocked"
        }
    }

    fun dismissAction(languageCode: String): String {
        return when (FunFactStrings.normalizedLanguage(languageCode)) {
            "sr" -> "Zatvori"
            else -> "Dismiss"
        }
    }

    fun fallbackFact(languageCode: String): String {
        return when (FunFactStrings.normalizedLanguage(languageCode)) {
            "sr" -> "Trenutno nema dostupne tajne zanimljivosti."
            else -> "No secret fact available right now."
        }
    }

    fun signalLabel(languageCode: String): String {
        return when (FunFactStrings.normalizedLanguage(languageCode)) {
            "sr" -> "LIVE SIGNAL"
            else -> "LIVE SIGNAL"
        }
    }

    fun signalStatus(languageCode: String): String {
        return when (FunFactStrings.normalizedLanguage(languageCode)) {
            "sr" -> "Presretanje signala..."
            else -> "Intercepting signal..."
        }
    }

    fun categoryLabel(categoryKey: String, languageCode: String): String {
        return when (normalizedCategoryKey(categoryKey)) {
            "algorithms" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Algoritmi" else "Algorithms"
            "computer-history" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Istorija racunara" else "Computer History"
            "networking" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Mreze" else "Networking"
            "languages" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Programski jezici" else "Languages"
            "databases" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Baze podataka" else "Databases"
            "web" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Veb" else "Web"
            "mobile" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Mobilni razvoj" else "Mobile"
            "security" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Bezbednost" else "Security"
            "tools" -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Alati" else "Tools"
            DEFAULT_CATEGORY_KEY -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Programiranje" else "Programming"
            else -> if (FunFactStrings.normalizedLanguage(languageCode) == "sr") "Programiranje" else "Programming"
        }
    }

    fun normalizedCategoryKey(rawCategory: String?): String {
        val normalized = rawCategory
            ?.trim()
            ?.lowercase()
            ?.replace('_', '-')
            ?.replace(' ', '-')
            .orEmpty()
        return normalized.ifBlank { DEFAULT_CATEGORY_KEY }
    }
}
