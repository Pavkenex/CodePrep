package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "cached_public_users")
data class CachedPublicUserEntity(
    @PrimaryKey val userId: String,
    val nickname: String,
    val level: Int,
    val avatarPresetId: String,
    val badgeIds: List<String>,
    val updatedAt: Instant?
)
