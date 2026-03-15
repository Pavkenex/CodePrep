package com.codeprep.app.data.local.entity

import androidx.room.Entity
import java.time.Instant

@Entity(
    tableName = "friend_requests",
    primaryKeys = ["ownerUserId", "requestUserId", "direction"]
)
data class FriendRequestEntity(
    val ownerUserId: String,
    val requestUserId: String,
    val direction: String,
    val createdAt: Instant?
)
