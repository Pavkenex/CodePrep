package com.codeprep.app.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.data.repository.FriendsRepository
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.TextLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendsScreen(
    onBack: () -> Unit,
    onFriendClick: (String) -> Unit,
    viewModel: AddFriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Friends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                placeholder = {
                    Text("Search by username")
                }
            )

            when {
                uiState.query.trim().length < FriendsRepository.MIN_SEARCH_LENGTH -> {
                    EmptyStateCard(
                        title = "Search by username",
                        subtitle = "Type at least ${FriendsRepository.MIN_SEARCH_LENGTH} characters to look for new friends."
                    )
                }

                uiState.isLoading && uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null && uiState.results.isEmpty() -> {
                    EmptyStateCard(
                        title = "Search unavailable",
                        subtitle = uiState.errorMessage.orEmpty()
                    )
                }

                uiState.results.isEmpty() -> {
                    EmptyStateCard(
                        title = "No users found",
                        subtitle = "Try a different username fragment."
                    )
                }

                else -> {
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextLight
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.results, key = { it.userId }) { user ->
                            SearchResultRow(
                                user = user,
                                isBusy = user.userId in uiState.actionInFlightIds,
                                onAdd = { viewModel.sendFriendRequest(user.userId) },
                                onAccept = { viewModel.acceptFriendRequest(user.userId) },
                                onOpenProfile = { onFriendClick(user.userId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
