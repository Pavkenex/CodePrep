package com.codeprep.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codeprep.app.data.local.dao.AiConversationDao
import com.codeprep.app.data.local.dao.AiResponseCacheDao
import com.codeprep.app.data.local.dao.CourseDao
import com.codeprep.app.data.local.dao.FriendsDao
import com.codeprep.app.data.local.dao.LessonProgressDao
import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.AiConversationEntity
import com.codeprep.app.data.local.entity.AiConversationMessageEntity
import com.codeprep.app.data.local.entity.AiResponseCacheEntity
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
        AiConversationEntity::class,
        AiConversationMessageEntity::class,
        AiResponseCacheEntity::class,
        CachedPublicUserEntity::class,
        FriendEntity::class,
        FriendRequestEntity::class

    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CodePrepDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun aiConversationDao(): AiConversationDao
    abstract fun aiResponseCacheDao(): AiResponseCacheDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun friendsDao(): FriendsDao
}
