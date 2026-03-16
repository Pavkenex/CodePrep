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
            // Koristimo sačuvani courseId za pristup pitanjima
            val questionsSnapshot = firestore.collection("modules")
                .document(cachedLesson.courseId)  // ← Ovo je ključno!
                .collection("lessons")
                .document(lessonId)
                .collection("questions")
                .orderBy("orderIndex")
                .get()
                .await()

            println("📝 Found ${questionsSnapshot.documents.size} questions")

            questionsSnapshot.documents.mapNotNull { document ->
                document.toQuestionOrNull()?.also {
                    println("✓ Question: ${it.text}")
                }
            }
        } catch (e: Exception) {
            println("❌ Error fetching questions: ${e.message}")
            emptyList()
        }
    }

    suspend fun getDailyQuestion(date: LocalDate = LocalDate.now()): Question? {
        val questions = try {
            val collectionGroupQuestions = firestore.collectionGroup("questions")
                .get()
                .await()
                .documents

            Log.d(TAG, "Daily challenge collectionGroup returned ${collectionGroupQuestions.size} docs")

            collectionGroupQuestions
                .mapNotNull { document -> document.toQuestionOrNull() }
                .filter { it.quizEnabled }
                .sortedWith(compareBy<Question> { it.randomKey }.thenBy { it.id })
        } catch (error: Exception) {
            Log.w(TAG, "Daily challenge collectionGroup query failed: ${error.message}", error)
            emptyList()
        }

        val resolvedQuestions = if (questions.isNotEmpty()) {
            questions
        } else {
            Log.d(TAG, "Falling back to modules/*/lessons/*/questions scan for daily challenge")
            fetchAllQuestionsFromModules()
        }

        if (resolvedQuestions.isEmpty()) {
            Log.w(TAG, "No daily challenge candidates found in Firestore")
            return null
        }

        val index = Math.floorMod(date.toEpochDay().toInt(), resolvedQuestions.size)
        val selectedQuestion = resolvedQuestions[index]
        Log.d(TAG, "Selected daily challenge ${selectedQuestion.id} for $date from ${resolvedQuestions.size} candidates")
        return selectedQuestion
    }

    private suspend fun fetchAllQuestionsFromModules(): List<Question> {
        return try {
            val modules = firestore.collection("modules")
                .orderBy("orderIndex")
                .get()
                .await()
                .documents

            val questions = mutableListOf<Question>()

            for (module in modules) {
                val lessons = module.reference.collection("lessons")
                    .orderBy("orderIndex")
                    .get()
                    .await()
                    .documents

                for (lesson in lessons) {
                    val lessonQuestions = lesson.reference.collection("questions")
                        .orderBy("orderIndex")
                        .get()
                        .await()
                        .documents
                        .mapNotNull { document -> document.toQuestionOrNull() }
                        .filter { it.quizEnabled }

                    questions += lessonQuestions
                }
            }

            Log.d(TAG, "Module scan found ${questions.size} daily challenge candidates")

            questions.sortedWith(compareBy<Question> { it.randomKey }.thenBy { it.id })
        } catch (error: Exception) {
            Log.w(TAG, "Module scan for daily challenge failed: ${error.message}", error)
            emptyList()
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

    private companion object {
        const val TAG = "CourseRepository"
    }
}
