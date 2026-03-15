package com.codeprep.app.data.local.entity

import androidx.room.Entity
import java.time.Instant

@Entity(
    tableName = "friends",
    primaryKeys = ["ownerUserId", "friendUserId"]
)
data class FriendEntity(
    val ownerUserId: String,
    val friendUserId: String,
    val addedAt: Instant?
)
