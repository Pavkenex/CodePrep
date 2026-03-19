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
import com.codeprep.app.R
import com.codeprep.app.ui.friends.AvatarBadge
import com.codeprep.app.ui.friends.AvatarPresets
import com.codeprep.app.ui.friends.BadgeStrip
import com.codeprep.app.ui.friends.EmptyStateCard
import com.codeprep.app.ui.friends.FriendsCarousel
import com.codeprep.app.ui.friends.LevelChip
import com.codeprep.app.ui.friends.ProfileViewModel
import com.codeprep.app.ui.friends.RequestCard
import com.codeprep.app.ui.localization.localizedStringResource
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
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: ProfileSettingsViewModel = hiltViewModel()
) {
    val progress by sessionViewModel.currentUserProgress.collectAsStateWithLifecycle()
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val selectedLanguage by settingsViewModel.selectedLanguage.collectAsStateWithLifecycle()
    var isAvatarSelectorVisible by remember { mutableStateOf(false) }
    var isSettingsVisible by remember { mutableStateOf(false) }

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
                    contentDescription = localizedStringResource(R.string.common_settings),
                    tint = LockedGrey,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { isSettingsVisible = true }
                )
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AvatarBadge(
                        nickname = progress?.nickname ?: localizedStringResource(R.string.common_user_fallback),
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
                                contentDescription = localizedStringResource(R.string.profile_edit_avatar),
                                tint = Charcoal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = progress?.nickname ?: localizedStringResource(R.string.common_user_fallback),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = IceWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeonStatCard(
                        label = localizedStringResource(R.string.profile_stat_xp),
                        value = "${progress?.xp ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                    NeonStatCard(
                        label = localizedStringResource(R.string.profile_stat_level),
                        value = "${progress?.level ?: 1}",
                        modifier = Modifier.weight(1f)
                    )
                    NeonStatCard(
                        label = localizedStringResource(R.string.profile_stat_friends),
                        value = "${uiState.friends.size}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            SectionHeader(title = localizedStringResource(R.string.common_badges))
        }

        item {
            BadgeStrip(badgeIds = uiState.badgeIds)
        }

        item {
            SectionHeader(title = localizedStringResource(R.string.profile_pending_requests))
        }

        if (uiState.pendingRequests.isEmpty()) {
            item {
                EmptyStateCard(
                    title = localizedStringResource(R.string.profile_pending_empty_title),
                    subtitle = localizedStringResource(R.string.profile_pending_empty_body)
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
                title = localizedStringResource(R.string.profile_friends_title),
                actionLabel = localizedStringResource(R.string.profile_add_friends),
                onActionClick = onAddFriends
            )
        }

        item {
            if (uiState.friends.isEmpty()) {
                EmptyStateCard(
                    title = localizedStringResource(R.string.profile_friends_empty_title),
                    subtitle = localizedStringResource(R.string.profile_friends_empty_body),
                    action = {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddFriends) {
                            Text(localizedStringResource(R.string.profile_add_friends))
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
                Text(localizedStringResource(R.string.profile_logout), color = CardinalRed)
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
                    text = localizedStringResource(R.string.profile_avatar_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = IceWhite
                )
                Text(
                    text = localizedStringResource(R.string.profile_avatar_sheet_body),
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
                                    nickname = progress?.nickname ?: localizedStringResource(R.string.common_user_fallback),
                                    avatarPresetId = preset.id,
                                    size = 72
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = localizedStringResource(preset.labelResId),
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

    if (isSettingsVisible) {
        ModalBottomSheet(
            onDismissRequest = { isSettingsVisible = false },
            containerColor = Charcoal
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = localizedStringResource(R.string.common_settings),
                    style = MaterialTheme.typography.titleLarge,
                    color = IceWhite
                )
                Text(
                    text = localizedStringResource(R.string.common_language),
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricCyan
                )
                LanguageOption(
                    label = localizedStringResource(R.string.profile_language_english),
                    isSelected = selectedLanguage == "en",
                    onClick = { settingsViewModel.setSelectedLanguage("en") }
                )
                LanguageOption(
                    label = localizedStringResource(R.string.profile_language_serbian),
                    isSelected = selectedLanguage == "sr",
                    onClick = { settingsViewModel.setSelectedLanguage("sr") }
                )
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

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(if (isSelected) ElectricCyan.copy(alpha = 0.12f) else Charcoal)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = IceWhite
        )
        if (isSelected) {
            Text(
                text = localizedStringResource(R.string.common_selected),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ElectricCyan
            )
        }
    }
}
