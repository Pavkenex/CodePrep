package com.codeprep.app.di

import android.content.Context
import androidx.room.Room
import com.codeprep.app.data.local.CodePrepDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CodePrepDatabase {
        return Room.databaseBuilder(
            context,
            CodePrepDatabase::class.java,
            "codeprep_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideUserProgressDao(db: CodePrepDatabase) = db.userProgressDao()

    @Provides
    fun provideAiExplanationDao(db: CodePrepDatabase) = db.aiExplanationDao()

    @Provides
    fun provideCourseDao(db: CodePrepDatabase) = db.courseDao()

    @Provides
    fun provideLessonProgressDao(db: CodePrepDatabase) = db.lessonProgressDao()

    @Provides
    fun provideFriendsDao(db: CodePrepDatabase) = db.friendsDao()
}
