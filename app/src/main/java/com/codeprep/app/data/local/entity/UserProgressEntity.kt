package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant


@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: String,
    val nickname: String,
    val xp: Int,
    val level: Int,
    var streak: Int,
    val hearts: Int,
    val lastHeartLostAt: Instant?,
    val lastActiveDate: Instant?,
    val updatedAt: Instant?
)
