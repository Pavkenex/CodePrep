package com.codeprep.app.data.repository

import android.util.Log
import com.codeprep.app.data.local.CodeSnippet
import com.codeprep.app.data.local.dao.CourseDao
import com.codeprep.app.data.local.entity.CachedCourseEntity
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.local.entity.Question
import com.codeprep.app.data.model.LessonContentBlock
import com.codeprep.app.data.model.LocalizedText
import com.codeprep.app.data.settings.AppSettingsStore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class CourseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val courseDao: CourseDao,
    private val appSettingsStore: AppSettingsStore
) {

    fun getCourses(): Flow<List<CachedCourseEntity>> {
        return courseDao.getAllCourses()
    }

    fun getAllLessons(): Flow<List<CachedLessonEntity>> {
        return courseDao.getAllLessons()
    }

    fun getLessonsForCourse(courseId: String): Flow<List<CachedLessonEntity>> {
        return courseDao.getLessonsForCourse(courseId)
    }

    suspend fun refreshCourses() {
        refreshModulesAndLessons()
    }

    suspend fun getCourseTitle(courseId: String): String? {
        return courseDao.getCourseById(courseId)
            ?.title
            ?.resolve(appSettingsStore.getSelectedLanguage())
            ?.takeIf { it.isNotBlank() }
    }

    suspend fun getLesson(lessonId: String): CachedLessonEntity? {
        val cached = courseDao.getLesson(lessonId)
        if (cached != null) return cached

        return try {
            val lesson = fetchLessonFromFirestore(lessonId) ?: return null
            courseDao.insertLesson(lesson)
            lesson
        } catch (error: Exception) {
            Log.w(TAG, "Failed to fetch lesson $lessonId", error)
            null
        }
    }

    suspend fun getQuestionsForLesson(lessonId: String): List<Question> {
        if (lessonId.isBlank()) return emptyList()

        val cachedLesson = courseDao.getLesson(lessonId) ?: fetchLessonFromFirestore(lessonId)?.also {
            courseDao.insertLesson(it)
        } ?: return emptyList()

        return try {
            fetchQuestionsForLessonFromFirestore(cachedLesson.courseId, lessonId)
        } catch (error: Exception) {
            Log.w(TAG, "Failed to fetch questions for lesson $lessonId", error)
            emptyList()
        }
    }

    suspend fun getDailyQuestion(date: LocalDate = LocalDate.now()): Question? {
        return try {
            fetchPublishedDailyChallenge(date) ?: selectFallbackDailyQuestionFromModules(date)
        } catch (error: Exception) {
            Log.w(TAG, "Daily challenge query failed: ${error.message}", error)
            null
        }
    }

    private suspend fun refreshModulesAndLessons() {
        try {
            val modules = fetchCoursesFromFirestore()
            courseDao.clearCourses()
            courseDao.insertAll(modules)
            modules.forEach { module ->
                refreshLessonsForCourse(module.courseId)
            }
        } catch (error: Exception) {
            Log.w(TAG, "Failed to refresh modules from Firestore", error)
        }
    }

    suspend fun refreshLessonsForCourse(courseId: String) {
        try {
            val lessons = fetchLessonsForCourseFromFirestore(courseId)
            courseDao.clearLessonsForCourse(courseId)
            if (lessons.isNotEmpty()) {
                courseDao.insertLessons(lessons)
            }
        } catch (error: Exception) {
            Log.w(TAG, "Failed to refresh lessons for module $courseId", error)
        }
    }

    private suspend fun fetchCoursesFromFirestore(): List<CachedCourseEntity> {
        val snapshot = firestore.collection("modules").get().await()
        return snapshot.documents
            .mapNotNull { document ->
                runCatching { document.toCachedCourseEntity() }
                    .onFailure { error ->
                        Log.w(TAG, "Skipping module ${document.id}: ${error.message}", error)
                    }
                    .getOrNull()
            }
            .sortedBy { it.orderIndex }
    }

    private suspend fun fetchLessonsForCourseFromFirestore(courseId: String): List<CachedLessonEntity> {
        val snapshot = firestore.collection("modules")
            .document(courseId)
            .collection("lessons")
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { document ->
                runCatching { document.toCachedLessonEntity() }
                    .onFailure { error ->
                        Log.w(TAG, "Skipping lesson ${document.id} for module $courseId: ${error.message}", error)
                    }
                    .getOrNull()
            }
            .sortedBy { it.orderIndex }
    }

    private suspend fun fetchLessonFromFirestore(lessonId: String): CachedLessonEntity? {
        val snapshot = firestore.collectionGroup("lessons")
            .whereEqualTo(FieldPath.documentId(), lessonId)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toCachedLessonEntity()
    }

    private fun DocumentSnapshot.toCachedCourseEntity(): CachedCourseEntity {
        return CachedCourseEntity(
            courseId = id,
            title = getLocalizedText("title"),
            description = getLocalizedText("description"),
            orderIndex = getLong("orderIndex")?.toInt() ?: 0,
            isLocked = getBoolean("isLocked") ?: false,
            lessonCount = getLong("lessonCount")?.toInt() ?: 0,
            icon = getString("icon").orEmpty()
        )
    }

    private fun DocumentSnapshot.toCachedLessonEntity(): CachedLessonEntity {
        val contentMap = get("content") as? Map<*, *> ?: emptyMap<String, Any>()
        return CachedLessonEntity(
            lessonId = id,
            courseId = getString("moduleId") ?: reference.parent.parent?.id.orEmpty(),
            title = getLocalizedText("title"),
            orderIndex = getLong("orderIndex")?.toInt() ?: 0,
            xpReward = getLong("xpReward")?.toInt() ?: 0,
            questionCount = getLong("questionCount")?.toInt() ?: 0,
            content = LessonContentBlock(
                introduction = contentMap["introduction"].toLocalizedText(),
                explanation = contentMap["explanation"].toLocalizedText(),
                example = contentMap["example"].toLocalizedText(),
                keyTakeaway = contentMap["keyTakeaway"].toLocalizedText()
            ),
            analogy = get("analogy")?.toLocalizedTextOrNull(),
            keyPoints = getLocalizedTextList("keyPoints"),
            commonMistakes = getLocalizedTextList("commonMistakes"),
            codeSnippets = getCodeSnippets("codeSnippets")
        )
    }

    private suspend fun selectFallbackDailyQuestionFromModules(date: LocalDate): Question? {
        val modules = fetchCoursesFromFirestore().sortedBy { it.orderIndex }
        if (modules.isEmpty()) {
            Log.w(TAG, "No modules found for daily challenge fallback")
            return null
        }

        val moduleStartIndex = cycleIndex(date.toEpochDay(), modules.size)
        for (moduleOffset in modules.indices) {
            val module = modules[(moduleStartIndex + moduleOffset) % modules.size]
            val lessons = fetchLessonsForCourseFromFirestore(module.courseId)
                .filter { it.questionCount > 0 }
                .sortedBy { it.orderIndex }

            if (lessons.isEmpty()) continue

            val lessonStartIndex = cycleIndex(date.toEpochDay() + moduleOffset, lessons.size)
            for (lessonOffset in lessons.indices) {
                val lesson = lessons[(lessonStartIndex + lessonOffset) % lessons.size]
                val questions = fetchQuestionsForLessonFromFirestore(module.courseId, lesson.lessonId)
                if (questions.isEmpty()) continue

                val questionIndex = cycleIndex(
                    date.toEpochDay() + moduleOffset + lessonOffset,
                    questions.size
                )
                return questions[questionIndex]
            }
        }

        Log.w(TAG, "No daily challenge candidates found in module fallback traversal")
        return null
    }

    private suspend fun fetchQuestionsForLessonFromFirestore(
        moduleId: String,
        lessonId: String
    ): List<Question> {
        val selectedLanguage = appSettingsStore.getSelectedLanguage()
        val snapshot = firestore.collection("modules")
            .document(moduleId)
            .collection("lessons")
            .document(lessonId)
            .collection("questions")
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { document -> document.toQuestionOrNull(selectedLanguage) }
            .sortedBy { it.orderIndex }
    }

    private fun DocumentSnapshot.toQuestionOrNull(selectedLanguage: String): Question? {
        val text = getLocalizedText("text").resolve(selectedLanguage)
        val rawOptions = get("options") as? List<*> ?: return null
        val options = rawOptions.mapNotNull { option ->
            option.toLocalizedTextOrNull()?.resolve(selectedLanguage)?.takeIf { it.isNotBlank() }
        }
        if (text.isBlank() || options.isEmpty()) return null

        val correctIndex = (getLong("correctIndex")?.toInt() ?: 0).coerceIn(0, options.lastIndex)
        return Question(
            id = id,
            moduleId = getString("moduleId") ?: reference.parent.parent?.parent?.parent?.id.orEmpty(),
            lessonId = getString("lessonId") ?: reference.parent.parent?.id.orEmpty(),
            lessonRefPath = reference.parent.parent?.path.orEmpty(),
            text = text,
            options = options,
            correctIndex = correctIndex,
            explanation = getLocalizedText("explanation").resolve(selectedLanguage),
            type = getString("type").orEmpty(),
            difficulty = "",
            difficultyWeight = 0.0,
            tags = emptyList(),
            format = "single_choice",
            quizEnabled = true,
            randomKey = 0L,
            orderIndex = getLong("orderIndex")?.toInt() ?: 0,
            seedVersion = "",
            codeSnippet = null
        )
    }

    private suspend fun fetchPublishedDailyChallenge(date: LocalDate): Question? {
        val activeOn = date.toString()
        val selectedLanguage = appSettingsStore.getSelectedLanguage()
        val documents = mutableListOf<DocumentSnapshot>()

        for (collectionName in DAILY_CHALLENGE_COLLECTIONS) {
            val fetchedDocuments = runCatching {
                firestore.collection(collectionName)
                    .whereEqualTo("activeOn", activeOn)
                    .limit(10)
                    .get()
                    .await()
                    .documents
            }.onFailure { error ->
                Log.w(TAG, "Daily challenge fetch failed for $collectionName: ${error.message}", error)
            }.getOrElse { emptyList() }

            documents += fetchedDocuments
        }

        if (documents.isEmpty()) return null

        val published = documents
            .filter { document -> document.getString("status").equals("published", ignoreCase = true) }
            .mapNotNull { document -> document.toDailyChallengeQuestionOrNull(selectedLanguage) }

        return published.firstOrNull()
    }

    private fun DocumentSnapshot.toDailyChallengeQuestionOrNull(selectedLanguage: String): Question? {
        return parseDailyChallengeQuestion(
            data = data.orEmpty(),
            selectedLanguage = selectedLanguage,
            fallbackId = id
        )
    }

    private fun DocumentSnapshot.getLocalizedText(field: String): LocalizedText {
        return get(field).toLocalizedTextOrNull() ?: LocalizedText()
    }

    private fun DocumentSnapshot.getLocalizedTextList(field: String): List<LocalizedText> {
        val rawItems = get(field) as? List<*> ?: return emptyList()
        return rawItems.mapNotNull { item -> item.toLocalizedTextOrNull() }
    }

    private fun DocumentSnapshot.getCodeSnippets(field: String): List<CodeSnippet> {
        val rawItems = get(field) as? List<*> ?: return emptyList()
        return rawItems.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            CodeSnippet(
                language = map["language"] as? String ?: "pseudocode",
                description = map["description"].toLocalizedText(),
                code = map["code"] as? String ?: return@mapNotNull null,
                isAntiPattern = map["isAntiPattern"] as? Boolean ?: false
            )
        }
    }

    private fun Any?.toLocalizedText(): LocalizedText {
        return toLocalizedTextOrNull() ?: LocalizedText()
    }

    private fun Any?.toLocalizedTextOrNull(): LocalizedText? {
        val map = this as? Map<*, *> ?: return null
        val en = (map["en"] as? String).orEmpty()
        val sr = (map["sr"] as? String).orEmpty()
        return if (en.isBlank() && sr.isBlank()) null else LocalizedText(en = en, sr = sr)
    }

    private fun cycleIndex(seed: Long, size: Int): Int {
        return Math.floorMod(seed, size.toLong()).toInt()
    }

    private companion object {
        const val TAG = "CourseRepository"
        val DAILY_CHALLENGE_COLLECTIONS = listOf("dailyChallanges", "dailyChallenges")
    }
}
