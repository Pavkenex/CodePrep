package com.codeprep.app.ui.home

import android.content.Context
import androidx.core.content.edit
import com.codeprep.app.data.local.entity.Question
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyChallengeStateStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun isCompleted(userId: String, date: LocalDate): Boolean {
        return prefs.getBoolean(completedKey(userId, date), false)
    }

    fun markCompleted(userId: String, date: LocalDate) {
        prefs.edit {
            putBoolean(completedKey(userId, date), true)
        }
    }

    fun getCachedQuestion(date: LocalDate): Question? {
        val payload = prefs.getString(questionKey(date), null) ?: return null
        return runCatching { gson.fromJson(payload, Question::class.java) }.getOrNull()
    }

    fun cacheQuestion(date: LocalDate, question: Question) {
        prefs.edit {
            putString(questionKey(date), gson.toJson(question))
        }
    }

    private fun completedKey(userId: String, date: LocalDate): String {
        val stableUserId = userId.ifBlank { "guest" }
        return "daily_challenge_${stableUserId}_${date}"
    }

    private fun questionKey(date: LocalDate): String = "daily_challenge_question_$date"

    companion object {
        private const val PREFS_NAME = "daily_challenge_state"
    }
}
