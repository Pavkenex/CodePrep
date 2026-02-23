package com.example.codeprep.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.codeprep.data.local.dao.AiExplanationDao
import com.example.codeprep.data.local.dao.UserProgressDao
import com.example.codeprep.data.local.entity.AiExplanationEntity
import com.example.codeprep.data.local.entity.CachedLessonEntity
import com.example.codeprep.data.local.entity.LessonProgressEntity
import com.example.codeprep.data.local.entity.UserProgressEntity

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