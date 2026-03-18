package com.codeprep.app.data.repository

import com.codeprep.app.data.model.DailyFunFact
import com.codeprep.app.data.model.FunFact
import com.codeprep.app.data.model.FunFactRotation
import com.codeprep.app.data.model.FunFactStrings
import com.codeprep.app.data.model.FunFactWidgetUiState
import com.codeprep.app.data.model.LocalizedText
import com.codeprep.app.data.model.SecretFactStrings
import com.codeprep.app.data.settings.AppSettingsStore
import com.codeprep.app.data.settings.FunFactCacheStore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FunFactRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val appSettingsStore: AppSettingsStore,
    private val cacheStore: FunFactCacheStore
) {
    fun getSelectedLanguage(): String {
        return FunFactStrings.normalizedLanguage(appSettingsStore.getSelectedLanguage())
    }

    suspend fun getTodayFact(zoneId: ZoneId = ZoneId.systemDefault()): DailyFunFact? {
        return getFactForDate(LocalDate.now(zoneId))
    }

    suspend fun getFactForDate(date: LocalDate): DailyFunFact? {
        val language = getSelectedLanguage()

        return try {
            val facts = loadFactsOrNull()

            if (facts.isNullOrEmpty()) {
                cachedFact()
            } else {
                val sortedFacts = FunFactRotation.sortFacts(facts)
                val fact = sortedFacts[FunFactRotation.indexForDate(date, sortedFacts.size)]
                val resolvedText = fact.content.resolve(language).trim()
                if (resolvedText.isBlank()) {
                    cachedFact()
                } else {
                    DailyFunFact(
                        id = fact.id,
                        text = resolvedText,
                        languageCode = language,
                        epochDay = date.toEpochDay(),
                        categoryKey = fact.categoryKey
                    ).also(cacheStore::save)
                }
            }
        } catch (_: Exception) {
            cachedFact()
        }
    }

    suspend fun getRandomFact(
        salt: Long,
        date: LocalDate = LocalDate.now()
    ): DailyFunFact? {
        val language = getSelectedLanguage()

        return try {
            val facts = loadFactsOrNull()
            if (facts.isNullOrEmpty()) {
                cachedFact()
            } else {
                val sortedFacts = FunFactRotation.sortFacts(facts)
                val fact = sortedFacts[FunFactRotation.randomIndex(date, sortedFacts.size, salt)]
                val resolvedText = fact.content.resolve(language).trim()
                if (resolvedText.isBlank()) {
                    cachedFact()
                } else {
                    DailyFunFact(
                        id = fact.id,
                        text = resolvedText,
                        languageCode = language,
                        epochDay = date.toEpochDay(),
                        categoryKey = fact.categoryKey
                    ).also(cacheStore::save)
                }
            }
        } catch (_: Exception) {
            cachedFact()
        }
    }

    suspend fun getWidgetState(isLoggedIn: Boolean): FunFactWidgetUiState {
        val language = getSelectedLanguage()
        if (!isLoggedIn) {
            return FunFactWidgetUiState(
                badge = FunFactStrings.badge(language),
                factText = FunFactStrings.neutralMessage(language),
                footer = FunFactStrings.footer(language),
                isLoggedIn = false
            )
        }

        val fact = getTodayFact()
        val message = fact?.text?.takeIf { it.isNotBlank() }
            ?: cacheStore.get()?.text?.takeIf { it.isNotBlank() }
            ?: FunFactStrings.fallbackMessage(language)

        return FunFactWidgetUiState(
            badge = FunFactStrings.badge(language),
            factText = message,
            footer = FunFactStrings.footer(language),
            isLoggedIn = true
        )
    }

    private fun cachedFact(): DailyFunFact? {
        val cached = cacheStore.get() ?: return null
        return DailyFunFact(
            id = cached.id,
            text = cached.text,
            languageCode = cached.languageCode,
            epochDay = cached.epochDay
        )
    }

    private fun DocumentSnapshot.toFunFactOrNull(): FunFact? {
        val id = id.takeIf { it.isNotBlank() } ?: return null
        val content = LocalizedText(
            en = getString("en").orEmpty(),
            sr = getString("sr").orEmpty()
        )
        if (content.en.isBlank() && content.sr.isBlank()) return null

        return FunFact(
            id = id,
            content = content,
            categoryKey = SecretFactStrings.normalizedCategoryKey(
                getString("categoryKey") ?: getString("category")
            )
        )
    }

    private suspend fun loadFactsOrNull(): List<FunFact>? {
        return firestore.collection(COLLECTION_NAME)
            .get()
            .await()
            .documents
            .mapNotNull { document -> document.toFunFactOrNull() }
    }

    companion object {
        private const val COLLECTION_NAME = "funFacts"
    }
}
