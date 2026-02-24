package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: String,
    val xp: Int,
    val level: Int,
    val streak: Int,
    val hearts: Int,
    val heartsLockedUntil: Long?,
    val lastActiveDate: Long,
    val updatedAt: Long
)
