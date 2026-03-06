package com.codeprep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.codeprep.app.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    // 1. Za UI (Strim koji stalno osvežava ekran)
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getUserProgressStream(userId: String): Flow<UserProgressEntity?>

    // 2. Za tvoju 'updateProgress' funkciju (Uzima podatak samo jednom)
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserProgressEntity?

    @Upsert // Modernija verzija @Insert(onConflict = REPLACE)
    suspend fun upsert(progress: UserProgressEntity)
}