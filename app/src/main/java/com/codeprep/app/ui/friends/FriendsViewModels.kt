package com.codeprep.app.ui.friends

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.R
import com.codeprep.app.data.friends.DEFAULT_AVATAR_PRESET_ID
import com.codeprep.app.data.friends.FriendRelationState
import com.codeprep.app.data.friends.PublicUserProfile
import com.codeprep.app.data.repository.FriendsRepository
import com.codeprep.app.data.settings.AppStringProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val avatarPresetId: String = DEFAULT_AVATAR_PRESET_ID,
    val badgeIds: List<String> = emptyList(),
    val pendingRequests: List<FriendListItemUiModel> = emptyList(),
    val friends: List<FriendListItemUiModel> = emptyList(),
    val isSyncing: Boolean = true,
    val isUpdatingAvatar: Boolean = false,
    val requestInFlightIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)

data class AddFriendsUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val results: List<SearchUserUiModel> = emptyList(),
    val actionInFlightIds: Set<String> = emptySet()
)

data class FriendProfileScreenState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val profile: FriendProfileUiModel? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
    private val appStringProvider: AppStringProvider,
    auth: FirebaseAuth
) : ViewModel() {
    private val userId = auth.currentUser?.uid

    private val isSyncing = MutableStateFlow(userId != null)
    private val isUpdatingAvatar = MutableStateFlow(false)
    private val requestInFlightIds = MutableStateFlow<Set<String>>(emptySet())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val socialState: Flow<Triple<PublicUserProfile?, List<PublicUserProfile>, List<PublicUserProfile>>> =
        if (userId == null) {
            flowOf(Triple(null, emptyList(), emptyList()))
        } else {
            combine(
                friendsRepository.observePublicProfile(userId),
                friendsRepository.observeIncomingRequests(userId),
                friendsRepository.observeFriends(userId)
            ) { ownProfile, requests, friends ->
                Triple(ownProfile, requests, friends)
            }
        }

    val uiState: StateFlow<ProfileUiState> = if (userId == null) {
        MutableStateFlow(ProfileUiState(isSyncing = false))
    } else {
        combine(
            socialState,
            isSyncing,
            isUpdatingAvatar,
            requestInFlightIds,
            errorMessage
        ) { social, syncing, updatingAvatar, inFlightIds, error ->
            val ownProfile = social.first
            val requests = social.second
            val friends = social.third
            ProfileUiState(
                avatarPresetId = ownProfile?.avatarPresetId ?: DEFAULT_AVATAR_PRESET_ID,
                badgeIds = ownProfile?.badgeIds.orEmpty(),
                pendingRequests = requests.map(PublicUserProfile::toListItemUiModel),
                friends = friends.map(PublicUserProfile::toListItemUiModel),
                isSyncing = syncing,
                isUpdatingAvatar = updatingAvatar,
                requestInFlightIds = inFlightIds,
                errorMessage = error
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
    }

    init {
        refresh()
    }

    fun refresh() {
        val currentUserId = userId ?: return
        viewModelScope.launch {
            isSyncing.value = true
            errorMessage.value = null
            runCatching {
                friendsRepository.syncSocialGraph(currentUserId)
            }.onFailure {
                errorMessage.value = it.message ?: appStringProvider.get(R.string.friends_error_refresh)
            }
            isSyncing.value = false
        }
    }

    fun acceptFriendRequest(requesterId: String) {
        mutateRequest(requesterId) {
            friendsRepository.acceptFriendRequest(userId.orEmpty(), requesterId)
        }
    }

    fun declineFriendRequest(requesterId: String) {
        mutateRequest(requesterId) {
            friendsRepository.declineFriendRequest(userId.orEmpty(), requesterId)
        }
    }

    fun updateAvatarPreset(avatarPresetId: String) {
        val currentUserId = userId ?: return
        viewModelScope.launch {
            isUpdatingAvatar.value = true
            errorMessage.value = null
            runCatching {
                friendsRepository.updateAvatarPreset(currentUserId, avatarPresetId)
            }.onFailure {
                errorMessage.value = it.message ?: appStringProvider.get(R.string.friends_error_update_avatar)
            }
            isUpdatingAvatar.value = false
        }
    }

    private fun mutateRequest(requesterId: String, action: suspend () -> Unit) {
        if (requesterId.isBlank() || userId == null) return
        viewModelScope.launch {
            requestInFlightIds.value = requestInFlightIds.value + requesterId
            errorMessage.value = null
            runCatching { action() }
                .onFailure {
                    errorMessage.value = it.message ?: appStringProvider.get(R.string.friends_error_request_action)
                }
            requestInFlightIds.value = requestInFlightIds.value - requesterId
        }
    }
}

@HiltViewModel
class AddFriendsViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
    private val appStringProvider: AppStringProvider,
    auth: FirebaseAuth
) : ViewModel() {
    private val userId = auth.currentUser?.uid
    private val _uiState = MutableStateFlow(AddFriendsUiState())
    val uiState: StateFlow<AddFriendsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        userId?.let { currentUserId ->
            viewModelScope.launch {
                runCatching { friendsRepository.syncSocialGraph(currentUserId) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query, errorMessage = null)
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(isLoading = false, results = emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(250)
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                friendsRepository.searchUsers(userId.orEmpty(), query)
            }.onSuccess { results ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = results.map { result ->
                        SearchUserUiModel(
                            userId = result.profile.userId,
                            nickname = result.profile.nickname,
                            level = result.profile.level,
                            avatarPresetId = result.profile.avatarPresetId,
                            relationState = result.relationState,
                            badgeIds = result.profile.badgeIds
                        )
                    }
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: appStringProvider.get(R.string.friends_error_search_users)
                )
            }
        }
    }

    fun sendFriendRequest(targetUserId: String) {
        mutateResultAction(targetUserId) {
            friendsRepository.sendFriendRequest(userId.orEmpty(), targetUserId)
            updateRelationState(targetUserId, FriendRelationState.OutgoingRequest)
        }
    }

    fun acceptFriendRequest(requesterId: String) {
        mutateResultAction(requesterId) {
            friendsRepository.acceptFriendRequest(userId.orEmpty(), requesterId)
            updateRelationState(requesterId, FriendRelationState.Friends)
        }
    }

    private fun mutateResultAction(targetUserId: String, action: suspend () -> Unit) {
        if (targetUserId.isBlank() || userId == null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                actionInFlightIds = _uiState.value.actionInFlightIds + targetUserId,
                errorMessage = null
            )
            runCatching { action() }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = it.message ?: appStringProvider.get(R.string.friends_error_update_status)
                    )
                }
            _uiState.value = _uiState.value.copy(
                actionInFlightIds = _uiState.value.actionInFlightIds - targetUserId
            )
        }
    }

    private fun updateRelationState(userId: String, relationState: FriendRelationState) {
        _uiState.value = _uiState.value.copy(
            results = _uiState.value.results.map { result ->
                if (result.userId == userId) result.copy(relationState = relationState) else result
            }
        )
    }
}

@HiltViewModel
class FriendProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val friendsRepository: FriendsRepository,
    private val appStringProvider: AppStringProvider
) : ViewModel() {
    private val friendId: String = savedStateHandle.get<String>("friendId").orEmpty()
    private val isLoading = MutableStateFlow(true)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FriendProfileScreenState> =
        combine(
            if (friendId.isBlank()) flowOf(null) else friendsRepository.observePublicProfile(friendId),
            isLoading,
            errorMessage
        ) { profile, loading, error ->
            FriendProfileScreenState(
                isLoading = loading,
                errorMessage = error,
                profile = profile?.let {
                    FriendProfileUiModel(
                        userId = it.userId,
                        nickname = it.nickname,
                        level = it.level,
                        avatarPresetId = it.avatarPresetId,
                        badgeIds = it.badgeIds
                    )
                }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FriendProfileScreenState())

    init {
        refresh()
    }

    fun refresh() {
        if (friendId.isBlank()) {
            errorMessage.value = appStringProvider.get(R.string.friend_profile_missing)
            isLoading.value = false
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            runCatching {
                friendsRepository.refreshPublicProfile(friendId)
            }.onFailure {
                errorMessage.value = it.message ?: appStringProvider.get(R.string.friends_error_load_profile)
            }
            isLoading.value = false
        }
    }
}

private fun PublicUserProfile.toListItemUiModel(): FriendListItemUiModel {
    return FriendListItemUiModel(
        userId = userId,
        nickname = nickname,
        level = level,
        avatarPresetId = avatarPresetId,
        badgeIds = badgeIds
    )
}
