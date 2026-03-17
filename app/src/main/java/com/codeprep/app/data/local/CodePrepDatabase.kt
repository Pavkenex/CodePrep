package com.codeprep.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codeprep.app.data.local.dao.AiExplanationDao
import com.codeprep.app.data.local.dao.CourseDao
import com.codeprep.app.data.local.dao.FriendsDao
import com.codeprep.app.data.local.dao.LessonProgressDao
import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.AiExplanationEntity
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.local.entity.CachedCourseEntity
import com.codeprep.app.data.local.entity.CachedPublicUserEntity
import com.codeprep.app.data.local.entity.FriendEntity
import com.codeprep.app.data.local.entity.FriendRequestEntity
import com.codeprep.app.data.local.entity.LessonProgressEntity
import com.codeprep.app.data.local.entity.UserProgressEntity

@Database(
    entities = [
        UserProgressEntity::class,
        LessonProgressEntity::class,
        CachedLessonEntity::class,
        CachedCourseEntity::class,
        AiExplanationEntity::class,
        CachedPublicUserEntity::class,
        FriendEntity::class,
        FriendRequestEntity::class

    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CodePrepDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun aiExplanationDao(): AiExplanationDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun friendsDao(): FriendsDao
}
