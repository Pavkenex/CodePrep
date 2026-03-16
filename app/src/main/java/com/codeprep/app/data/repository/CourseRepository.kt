package com.codeprep.app.data.repository

import android.util.Log
import com.codeprep.app.data.local.CodeSnippet
import com.codeprep.app.data.local.dao.CourseDao
import com.codeprep.app.data.local.entity.CachedCourseEntity
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.local.entity.Question
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

class CourseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val courseDao: CourseDao
) {

    fun getCourses(): Flow<List<CachedCourseEntity>> {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val courses = fetchCoursesFromFirestore()
                courseDao.insertAll(courses)
            } catch (_: Exception) {
                // Offline or network error -> keep serving Room cache.
            }
        }

        return courseDao.getAllCourses()
    }

    fun getLessonsForCourse(courseId: String): Flow<List<CachedLessonEntity>> {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lessons = fetchLessonsForCourseFromFirestore(courseId)
                if (lessons.isNotEmpty()) {
                    courseDao.insertLessons(lessons)
                }
            } catch (_: Exception) {
                // Offline or network error -> keep serving Room cache.
            }
        }

        return courseDao.getLessonsForCourse(courseId)
    }

    private suspend fun fetchCoursesFromFirestore(): List<CachedCourseEntity> {
        val moduleSnapshot = firestore.collection("modules")
            .orderBy("orderIndex")
            .get()
            .await()
        return moduleSnapshot.map { it.toCachedCourseEntity() }
    }

    private suspend fun fetchLessonsForCourseFromFirestore(courseId: String): List<CachedLessonEntity> {

        val primarySnapshot = firestore.collection("modules")
            .document(courseId)
            .collection("lessons")
            .orderBy("orderIndex")
            .get()
            .await()

        return primarySnapshot.documents.map { it.toCachedLessonEntity() }
    }

    private fun DocumentSnapshot.toCachedCourseEntity(): CachedCourseEntity {
        return CachedCourseEntity(
            courseId = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            order = getLong("order")?.toInt() ?: getLong("orderIndex")?.toInt() ?: 0
        )
    }

    suspend fun getCourseTitle(courseId: String): String? {
        return courseDao.getCourseById(courseId)?.title?.takeIf { it.isNotBlank() }
    }

    suspend fun getLesson(lessonId: String): CachedLessonEntity? {
        val cached = courseDao.getLesson(lessonId)
        if (cached != null) return cached

        return try {
            val lesson = fetchLessonFromFirestore(lessonId) ?: return null
            courseDao.insertLesson(lesson)
            lesson
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchLessonFromFirestore(lessonId: String): CachedLessonEntity? {
        val snapshot = firestore.collectionGroup("lessons")
            .whereEqualTo(FieldPath.documentId(), lessonId)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toCachedLessonEntity()
    }

    private fun DocumentSnapshot.toCachedLessonEntity(): CachedLessonEntity {
        val summary = getString("theorySummary") ?: getString("content") ?: ""
        return CachedLessonEntity(
            lessonId = id,
            courseId = getString("courseId")
                ?: getString("moduleId")
                ?: reference.parent.parent?.id.orEmpty(),
            title = getString("title") ?: "",
            content = summary,
            analogy = getString("analogy"),
            keyPoints = getStringList("keyPoints"),
            commonMistakes = getStringList("commonMistakes"),
            codeSnippets = getCodeSnippets("codeSnippets"),
            orderIndex = getLong("order")?.toInt() ?: getLong("orderIndex")?.toInt() ?: 0,
            xpReward = getLong("xpReward")?.toInt() ?: 0,
            questionCount = getLong("questionCount")?.toInt() ?: 0
        )
    }

    private fun DocumentSnapshot.getStringList(field: String): List<String> {
        return (get(field) as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    }

    private fun DocumentSnapshot.getCodeSnippets(field: String): List<CodeSnippet> {
        val rawItems = get(field) as? List<*> ?: return emptyList()
        return rawItems.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            CodeSnippet(
                language = map["language"] as? String ?: "",
                description = map["description"] as? String ?: "",
                code = map["code"] as? String ?: "",
                isAntiPattern = map["isAntiPattern"] as? Boolean ?: false
            )
        }
    }

    suspend fun getQuestionsForLesson(lessonId: String): List<Question> {
        if (lessonId.isBlank()) return emptyList()

        // Prvo dobavi lekciju iz lokalne baze da dobijemo courseId
        val cachedLesson = courseDao.getLesson(lessonId)
        if (cachedLesson == null) {
            println("❌ Lesson not found in cache: $lessonId")
            return emptyList()
        }

        println("✅ Found lesson: ${cachedLesson.title} with courseId: ${cachedLesson.courseId}")

        return try {
            val questions = fetchQuestionsForLessonFromFirestore(cachedLesson.courseId, lessonId)
            println("📝 Found ${questions.size} questions")
            questions.onEach {
                println("✓ Question: ${it.text}")
            }
        } catch (e: Exception) {
            println("❌ Error fetching questions: ${e.message}")
            emptyList()
        }
    }

    suspend fun getDailyQuestion(date: LocalDate = LocalDate.now()): Question? {
        return try {
            fetchPublishedDailyChallenge(date)?.also { challenge ->
                Log.d(TAG, "Selected daily challenge ${challenge.id} from daily challenge collection")
                return challenge
            }

            selectFallbackDailyQuestionFromModules(date)
        } catch (error: Exception) {
            Log.w(TAG, "Daily challenge query failed: ${error.message}", error)
            null
        }
    }

    private suspend fun fetchPublishedDailyChallenge(date: LocalDate): Question? {
        val activeOn = date.toString()
        val localeTag = Locale.getDefault().toLanguageTag()
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

        if (documents.isEmpty()) {
            Log.d(TAG, "No daily challenge documents found for $activeOn")
            return null
        }

        val published = documents
            .filter { document -> document.getString("status").equals("published", ignoreCase = true) }
            .mapNotNull { document -> document.toDailyChallengeQuestionOrNull() }

        val localized = published.firstOrNull { it.lessonRefPath == localeTag }
        return localized ?: published.firstOrNull()
    }

    private suspend fun selectFallbackDailyQuestionFromModules(date: LocalDate): Question? {
        val modules = fetchCoursesFromFirestore()
            .sortedBy { it.order }

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

            if (lessons.isEmpty()) {
                continue
            }

            val lessonStartIndex = cycleIndex(date.toEpochDay() + moduleOffset, lessons.size)
            for (lessonOffset in lessons.indices) {
                val lesson = lessons[(lessonStartIndex + lessonOffset) % lessons.size]
                val questions = fetchQuestionsForLessonFromFirestore(module.courseId, lesson.lessonId)
                    .filter { it.quizEnabled }

                if (questions.isEmpty()) {
                    continue
                }

                val questionIndex = cycleIndex(
                    date.toEpochDay() + moduleOffset + lessonOffset,
                    questions.size
                )
                val selectedQuestion = questions[questionIndex]
                Log.d(
                    TAG,
                    "Selected daily challenge fallback ${selectedQuestion.id} from module ${module.courseId}, lesson ${lesson.lessonId}"
                )
                return selectedQuestion
            }
        }

        Log.w(TAG, "No daily challenge candidates found in module fallback traversal")
        return null
    }

    private suspend fun fetchQuestionsForLessonFromFirestore(
        moduleId: String,
        lessonId: String
    ): List<Question> {
        val questionsSnapshot = firestore.collection("modules")
            .document(moduleId)
            .collection("lessons")
            .document(lessonId)
            .collection("questions")
            .orderBy("orderIndex")
            .get()
            .await()

        return questionsSnapshot.documents.mapNotNull { document ->
            document.toQuestionOrNull()?.also {
                println("✓ Question: ${it.text}")
            }
        }
    }

    private fun DocumentSnapshot.toQuestionOrNull(): Question? {
        val text = getString("text") ?: return null
        val options = (get("options") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        if (options.isEmpty()) return null

        val correctIndex = getLong("correctIndex")?.toInt()
            ?: getLong("answerIndex")?.toInt()
            ?: 0

        val snippet = getString("codeSnippet")
            ?: getString("snippet")
            ?: getString("code")

        return Question(
            id = id,
            title = getString("title") ?: "",
            moduleId = getString("moduleId")
                ?: getString("courseId")
                ?: reference.parent.parent?.parent?.parent?.id.orEmpty(),
            lessonId = getString("lessonId")
                ?: reference.parent.parent?.id.orEmpty(),
            lessonRefPath = getString("lessonRefPath")
                ?: reference.parent.parent?.path.orEmpty(),
            text = text,
            options = options,
            correctIndex = correctIndex.coerceIn(0, options.lastIndex),
            explanation = getString("explanation") ?: "",
            type = getString("type") ?: "",
            difficulty = getString("difficulty") ?: "",
            difficultyWeight = getDouble("difficultyWeight") ?: 0.0,
            tags = getStringList("tags"),
            format = getString("format") ?: "single_choice",
            quizEnabled = getBoolean("quizEnabled") ?: true,
            randomKey = getLong("randomKey") ?: 0L,
            orderIndex = getLong("orderIndex")?.toInt() ?: 0,
            seedVersion = getString("seedVersion") ?: "",
            codeSnippet = snippet?.takeIf { it.isNotBlank() }
        )
    }

    private fun DocumentSnapshot.toDailyChallengeQuestionOrNull(): Question? {
        val prompt = getString("prompt") ?: return null
        val options = (get("options") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        if (options.isEmpty()) return null

        val correctIndexes = (get("correctOptionIndexes") as? List<*>)?.mapNotNull { raw ->
            when (raw) {
                is Long -> raw.toInt()
                is Int -> raw
                is Double -> raw.toInt()
                else -> null
            }
        }.orEmpty()

        val correctIndex = correctIndexes.firstOrNull()?.coerceIn(0, options.lastIndex) ?: 0
        val locale = getString("locale").orEmpty()
        val selectionMode = getString("selectionMode").orEmpty()

        return Question(
            id = getString("id") ?: id,
            title = getString("title") ?: "",
            moduleId = "",
            lessonId = "",
            lessonRefPath = locale,
            text = prompt,
            options = options,
            correctIndex = correctIndex,
            explanation = getString("explanation") ?: getString("description") ?: "",
            type = getString("challengeType") ?: "",
            difficulty = getString("difficulty") ?: "",
            difficultyWeight = getLong("pointsReward")?.toDouble() ?: 0.0,
            tags = getStringList("tags"),
            format = when (selectionMode.lowercase()) {
                "single" -> "single_choice"
                else -> selectionMode.ifBlank { "single_choice" }
            },
            quizEnabled = true,
            randomKey = 0L,
            orderIndex = 0,
            seedVersion = getString("seedVersion") ?: "",
            codeSnippet = getString("codeSnippet")?.takeIf { it.isNotBlank() }
        )
    }

    private companion object {
        const val TAG = "CourseRepository"
        val DAILY_CHALLENGE_COLLECTIONS = listOf("dailyChallanges", "dailyChallenges")
    }

    private fun cycleIndex(seed: Long, size: Int): Int {
        return Math.floorMod(seed, size.toLong()).toInt()
    }
}
