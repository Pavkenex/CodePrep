package com.codeprep.app.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.R
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.IceWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    onBack: () -> Unit,
    viewModel: FriendProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(localizedStringResource(R.string.friend_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizedStringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.profile == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBackground)
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.profile != null -> {
                val profile = uiState.profile ?: return@Scaffold
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBackground)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarBadge(
                        nickname = profile.nickname,
                        avatarPresetId = profile.avatarPresetId,
                        size = 120
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = profile.nickname,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = IceWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LevelChip(level = profile.level)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = localizedStringResource(R.string.common_badges),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = IceWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BadgeStrip(badgeIds = profile.badgeIds)
                    uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { error ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IceWhite
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBackground)
                        .padding(paddingValues)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyStateCard(
                        title = localizedStringResource(R.string.friend_profile_unavailable_title),
                        subtitle = uiState.errorMessage
                            ?: localizedStringResource(R.string.friend_profile_unavailable_body)
                    )
                }
            }
        }
    }
}
