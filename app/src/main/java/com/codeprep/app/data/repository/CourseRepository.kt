package com.codeprep.app.data.repository

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
            text = text,
            options = options,
            correctIndex = correctIndex.coerceIn(0, options.lastIndex),
            explanation = getString("explanation") ?: "",
            codeSnippet = snippet?.takeIf { it.isNotBlank() }
        )
    }
}
