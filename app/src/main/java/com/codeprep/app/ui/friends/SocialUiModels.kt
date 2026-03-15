package com.codeprep.app.ui.friends

import androidx.compose.runtime.Immutable
import com.codeprep.app.data.friends.FriendRelationState

@Immutable
data class FriendListItemUiModel(
    val userId: String,
    val nickname: String,
    val level: Int,
    val avatarPresetId: String,
    val badgeIds: List<String>
)

@Immutable
data class SearchUserUiModel(
    val userId: String,
    val nickname: String,
    val level: Int,
    val avatarPresetId: String,
    val relationState: FriendRelationState,
    val badgeIds: List<String>
)

@Immutable
data class FriendProfileUiModel(
    val userId: String,
    val nickname: String,
    val level: Int,
    val avatarPresetId: String,
    val badgeIds: List<String>
)
