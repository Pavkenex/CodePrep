package com.codeprep.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codeprep.app.data.local.dao.AiExplanationDao
import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.AiExplanationEntity
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.local.entity.LessonProgressEntity
import com.codeprep.app.data.local.entity.UserProgressEntity

@Database(
    entities = [
        UserProgressEntity::class,
        LessonProgressEntity::class,
        CachedLessonEntity::class,
        AiExplanationEntity::class

    ],
    version = 1,
    exportSchema = false
)
abstract class CodePrepDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun aiExplanationDao(): AiExplanationDao
}