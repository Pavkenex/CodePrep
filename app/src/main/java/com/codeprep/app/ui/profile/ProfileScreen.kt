package com.codeprep.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.ui.friends.AvatarBadge
import com.codeprep.app.ui.friends.AvatarPresets
import com.codeprep.app.ui.friends.BadgeStrip
import com.codeprep.app.ui.friends.EmptyStateCard
import com.codeprep.app.ui.friends.FriendsCarousel
import com.codeprep.app.ui.friends.LevelChip
import com.codeprep.app.ui.friends.ProfileViewModel
import com.codeprep.app.ui.friends.RequestCard
import com.codeprep.app.ui.navigation.SessionBootstrapViewModel
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.CardinalRed
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.TextLight
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onAddFriends: () -> Unit,
    onFriendClick: (String) -> Unit,
    onLogout: () -> Unit,
    sessionViewModel: SessionBootstrapViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val progress by sessionViewModel.currentUserProgress.collectAsStateWithLifecycle()
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    var isAvatarSelectorVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .background(AppBackground)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = LockedGrey,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AvatarBadge(
                        nickname = progress?.nickname ?: "User",
                        avatarPresetId = uiState.avatarPresetId,
                        size = 120
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(ElectricCyan)
                            .clickable(enabled = !uiState.isUpdatingAvatar) {
                                isAvatarSelectorVisible = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isUpdatingAvatar) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = AppBackground
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit avatar",
                                tint = Charcoal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = progress?.nickname ?: "User",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = IceWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeonStatCard(label = "XP", value = "${progress?.xp ?: 0}", modifier = Modifier.weight(1f))
                    NeonStatCard(label = "Level", value = "${progress?.level ?: 1}", modifier = Modifier.weight(1f))
                    NeonStatCard(label = "Friends", value = "${uiState.friends.size}", modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            SectionHeader(title = "Badges")
        }

        item {
            BadgeStrip(badgeIds = uiState.badgeIds)
        }

        item {
            SectionHeader(title = "Pending Requests")
        }

        if (uiState.pendingRequests.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No pending requests",
                    subtitle = "Incoming friend requests will show up here."
                )
            }
        } else {
            items(uiState.pendingRequests, key = { it.userId }) { request ->
                RequestCard(
                    request = request,
                    isBusy = request.userId in uiState.requestInFlightIds,
                    onAccept = { profileViewModel.acceptFriendRequest(request.userId) },
                    onDecline = { profileViewModel.declineFriendRequest(request.userId) },
                    onOpenProfile = { onFriendClick(request.userId) }
                )
            }
        }

        item {
            SectionHeader(
                title = "Friends",
                actionLabel = "Add Friends",
                onActionClick = onAddFriends
            )
        }

        item {
            if (uiState.friends.isEmpty()) {
                EmptyStateCard(
                    title = "Build your circle",
                    subtitle = "Search by username and send your first friend request.",
                    action = {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddFriends) {
                            Text("Add Friends")
                        }
                    }
                )
            } else {
                FriendsCarousel(
                    friends = uiState.friends,
                    onFriendClick = onFriendClick
                )
            }
        }

        uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { error ->
            item {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLight
                )
            }
        }

        item {
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text("SYSTEM SHUTDOWN", color = CardinalRed)
            }
        }
    }

    if (isAvatarSelectorVisible) {
        ModalBottomSheet(
            onDismissRequest = { isAvatarSelectorVisible = false },
            containerColor = Charcoal
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose avatar preset",
                    style = MaterialTheme.typography.titleLarge,
                    color = IceWhite
                )
                Text(
                    text = "Preset art is still TODO. For now each preset maps to a distinct in-app style.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLight
                )
                AvatarPresets.chunked(3).forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowPresets.forEach { preset ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        profileViewModel.updateAvatarPreset(preset.id)
                                        isAvatarSelectorVisible = false
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarBadge(
                                    nickname = progress?.nickname ?: "User",
                                    avatarPresetId = preset.id,
                                    size = 72
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = IceWhite
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = IceWhite
            )
        )

        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                ),
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
private fun NeonStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(Charcoal)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
        )
    }
}
