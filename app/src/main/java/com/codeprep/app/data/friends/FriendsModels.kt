package com.codeprep.app.data.friends

import com.codeprep.app.data.local.entity.CachedPublicUserEntity
import java.time.Instant

data class PublicUserProfile(
    val userId: String,
    val nickname: String,
    val level: Int,
    val avatarPresetId: String,
    val badgeIds: List<String>,
    val updatedAt: Instant?
)

enum class FriendRequestDirection(val value: String) {
    Incoming("incoming"),
    Outgoing("outgoing")
}

enum class FriendRelationState {
    None,
    Friends,
    IncomingRequest,
    OutgoingRequest
}

data class SearchUserResult(
    val profile: PublicUserProfile,
    val relationState: FriendRelationState
)

fun CachedPublicUserEntity.toPublicUserProfile(): PublicUserProfile {
    return PublicUserProfile(
        userId = userId,
        nickname = nickname,
        level = level,
        avatarPresetId = avatarPresetId,
        badgeIds = badgeIds,
        updatedAt = updatedAt
    )
}
