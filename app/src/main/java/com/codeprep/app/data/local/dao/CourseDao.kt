package com.codeprep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codeprep.app.data.local.entity.CachedCourseEntity
import com.codeprep.app.data.local.entity.CachedLessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    // Kursevi
    @Query("SELECT * FROM cached_courses ORDER BY orderIndex ASC")
    fun getAllCourses(): Flow<List<CachedCourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<CachedCourseEntity>)

    @Query("SELECT * FROM cached_courses WHERE courseId = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: String): CachedCourseEntity?

    // Lekcije
    @Query("SELECT * FROM cached_lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<CachedLessonEntity>>

    @Query("SELECT * FROM cached_lessons ORDER BY courseId ASC, orderIndex ASC")
    fun getAllLessons(): Flow<List<CachedLessonEntity>>

    @Query("SELECT * FROM cached_lessons WHERE courseId = :courseId ORDER BY orderIndex ASC LIMIT 1")
    suspend fun getFirstLessonForCourse(courseId: String): CachedLessonEntity?

    // Dohvatanje jedne lekcije (verovatno će ti trebati za Lesson Screen)
    @Query("SELECT * FROM cached_lessons WHERE lessonId = :lessonId")
    suspend fun getLessonById(lessonId: String): CachedLessonEntity?

    @Query("SELECT * FROM cached_lessons WHERE lessonId = :lessonId")
    suspend fun getLesson(lessonId: String): CachedLessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<CachedLessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: CachedLessonEntity)

    // Opciono: Brisanje starih podataka pre insert-a (refresh logika)
    @Query("DELETE FROM cached_courses")
    suspend fun clearCourses()

    @Query("DELETE FROM cached_lessons WHERE courseId = :courseId")
    suspend fun clearLessonsForCourse(courseId: String)
}
