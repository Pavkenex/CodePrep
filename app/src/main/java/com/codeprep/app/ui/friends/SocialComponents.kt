package com.codeprep.app.ui.friends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.codeprep.app.R
import com.codeprep.app.data.friends.FriendRelationState
import com.codeprep.app.data.friends.DEFAULT_AVATAR_PRESET_ID
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.CardinalRed
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.DeepCharcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.LockedGreyDark
import com.codeprep.app.ui.theme.SkyBlue
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight

data class AvatarPresetUi(
    val id: String,
    @StringRes val labelResId: Int,
    val colors: List<Color>
)

val AvatarPresets = listOf(
    AvatarPresetUi("avatar_01", R.string.avatar_preset_pulse, listOf(ElectricCyan, SkyBlue)),
    AvatarPresetUi("avatar_02", R.string.avatar_preset_core, listOf(SunYellow, ElectricCyan)),
    AvatarPresetUi("avatar_03", R.string.avatar_preset_volt, listOf(CardinalRed, SunYellow)),
    AvatarPresetUi("avatar_04", R.string.avatar_preset_trace, listOf(SkyBlue, LockedGreyDark)),
    AvatarPresetUi("avatar_05", R.string.avatar_preset_flux, listOf(ElectricCyan, LockedGreyDark)),
    AvatarPresetUi("avatar_06", R.string.avatar_preset_kernel, listOf(SunYellow, CardinalRed))
)

@Composable
fun AvatarBadge(
    nickname: String,
    avatarPresetId: String,
    modifier: Modifier = Modifier,
    size: Int = 72
) {
    val preset = AvatarPresets.firstOrNull { it.id == avatarPresetId }
        ?: AvatarPresets.first { it.id == DEFAULT_AVATAR_PRESET_ID }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(preset.colors)
            )
            .border(3.dp, ElectricCyan.copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // TODO: Replace initial-based avatars with bundled illustrated avatar assets per preset ID.
        Text(
            text = nickname.take(1).uppercase().ifBlank { "U" },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = AppBackground
            )
        )
    }
}

@Composable
fun BadgeStrip(
    badgeIds: List<String>,
    modifier: Modifier = Modifier,
    slotCount: Int = 4
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(slotCount) { index ->
            val badgeId = badgeIds.getOrNull(index)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (badgeId == null) Charcoal else DeepCharcoal)
                    .border(
                        1.dp,
                        if (badgeId == null) LockedGreyDark else ElectricCyan.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (badgeId == null) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = localizedStringResource(R.string.social_locked_badge),
                        tint = LockedGreyDark
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = localizedStringResource(R.string.social_badge),
                            tint = SunYellow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = badgeId.take(6).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = IceWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsCarousel(
    friends: List<FriendListItemUiModel>,
    onFriendClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(friends, key = { it.userId }) { friend ->
            FriendCarouselCard(
                friend = friend,
                onFriendClick = onFriendClick
            )
        }
    }
}

@Composable
private fun FriendCarouselCard(
    friend: FriendListItemUiModel,
    onFriendClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .width(156.dp)
            .testTag("friend-card-${friend.userId}")
            .clickable { onFriendClick(friend.userId) },
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarBadge(
                nickname = friend.nickname,
                avatarPresetId = friend.avatarPresetId,
                size = 60
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = friend.nickname,
                style = MaterialTheme.typography.titleSmall,
                color = IceWhite,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            LevelChip(level = friend.level)
        }
    }
}

@Composable
fun RequestCard(
    request: FriendListItemUiModel,
    isBusy: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenProfile),
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarBadge(
                nickname = request.nickname,
                avatarPresetId = request.avatarPresetId,
                size = 52
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = request.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        color = IceWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LevelChip(level = request.level)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        enabled = !isBusy
                    ) {
                        Text(localizedStringResource(R.string.common_decline))
                    }
                    Button(
                        onClick = onAccept,
                        enabled = !isBusy
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AppBackground
                            )
                        } else {
                            Text(localizedStringResource(R.string.common_accept))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    user: SearchUserUiModel,
    isBusy: Boolean,
    onAdd: () -> Unit,
    onAccept: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenProfile),
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarBadge(
                nickname = user.nickname,
                avatarPresetId = user.avatarPresetId,
                size = 52
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = IceWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                LevelChip(level = user.level)
            }
            FriendActionButton(
                relationState = user.relationState,
                isBusy = isBusy,
                onAdd = onAdd,
                onAccept = onAccept
            )
        }
    }
}

@Composable
fun FriendActionButton(
    relationState: FriendRelationState,
    isBusy: Boolean,
    onAdd: () -> Unit,
    onAccept: () -> Unit
) {
    when (relationState) {
        FriendRelationState.None -> Button(onClick = onAdd, enabled = !isBusy) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = AppBackground
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(localizedStringResource(R.string.common_add))
            }
        }

        FriendRelationState.IncomingRequest -> Button(onClick = onAccept, enabled = !isBusy) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = AppBackground
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(localizedStringResource(R.string.common_accept))
            }
        }

        FriendRelationState.OutgoingRequest -> AssistChip(
            onClick = {},
            enabled = false,
            label = { Text(localizedStringResource(R.string.social_requested)) },
            colors = AssistChipDefaults.assistChipColors(
                disabledContainerColor = DeepCharcoal,
                disabledLabelColor = ElectricCyan
            )
        )

        FriendRelationState.Friends -> AssistChip(
            onClick = {},
            enabled = false,
            label = { Text(localizedStringResource(R.string.social_friends)) },
            colors = AssistChipDefaults.assistChipColors(
                disabledContainerColor = DeepCharcoal,
                disabledLabelColor = IceWhite
            )
        )
    }
}

@Composable
fun LevelChip(level: Int) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = DeepCharcoal,
        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.35f))
    ) {
        Text(
            text = localizedStringResource(R.string.common_level_value, level),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ElectricCyan
        )
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        border = BorderStroke(1.dp, LockedGreyDark.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = IceWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight
            )
            action?.invoke()
        }
    }
}
