package com.codeprep.app.data.repository

import com.codeprep.app.data.friends.DEFAULT_AVATAR_PRESET_ID
import com.codeprep.app.data.friends.FriendRelationState
import com.codeprep.app.data.friends.FriendRequestDirection
import com.codeprep.app.data.friends.PublicUserProfile
import com.codeprep.app.data.friends.SearchUserResult
import com.codeprep.app.data.friends.buildNicknameSearchTerms
import com.codeprep.app.data.friends.toPublicUserProfile
import com.codeprep.app.data.local.dao.FriendsDao
import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.CachedPublicUserEntity
import com.codeprep.app.data.local.entity.FriendEntity
import com.codeprep.app.data.local.entity.FriendRequestEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val friendsDao: FriendsDao,
    private val userProgressDao: UserProgressDao
) {
    fun observeFriends(ownerUserId: String): Flow<List<PublicUserProfile>> {
        return friendsDao.observeFriends(ownerUserId).map { users ->
            users.map(CachedPublicUserEntity::toPublicUserProfile)
        }
    }

    fun observeIncomingRequests(ownerUserId: String): Flow<List<PublicUserProfile>> {
        return friendsDao.observeRequests(ownerUserId, FriendRequestDirection.Incoming.value).map { users ->
            users.map(CachedPublicUserEntity::toPublicUserProfile)
        }
    }

    fun observePublicProfile(userId: String): Flow<PublicUserProfile?> {
        return friendsDao.observePublicUser(userId).map { it?.toPublicUserProfile() }
    }

    suspend fun syncSocialGraph(ownerUserId: String) {
        if (ownerUserId.isBlank()) return

        val fallbackProgress = userProgressDao.getUserById(ownerUserId)
        val userRef = firestore.collection(USERS_COLLECTION).document(ownerUserId)
        val userSnapshot = userRef.get().await()

        val currentProfile = if (userSnapshot.exists()) {
            userSnapshot.toCachedPublicUser(
                userId = ownerUserId,
                fallbackNickname = fallbackProgress?.nickname.orEmpty(),
                fallbackLevel = fallbackProgress?.level ?: 1
            )
        } else {
            CachedPublicUserEntity(
                userId = ownerUserId,
                nickname = fallbackProgress?.nickname.orEmpty(),
                level = fallbackProgress?.level ?: 1,
                avatarPresetId = DEFAULT_AVATAR_PRESET_ID,
                badgeIds = emptyList(),
                updatedAt = Instant.now()
            )
        }

        val friendIds = userSnapshot.getStringList(FIELD_FRIENDS)
        val incomingIds = userSnapshot.getStringList(FIELD_INCOMING_REQUESTS)
        val outgoingIds = userSnapshot.getStringList(FIELD_OUTGOING_REQUESTS)

        friendsDao.upsertPublicUser(currentProfile)
        ensureRemoteDefaults(
            ownerUserId = ownerUserId,
            profile = currentProfile,
            friendIds = friendIds,
            incomingIds = incomingIds,
            outgoingIds = outgoingIds
        )

        syncFriendIds(ownerUserId, friendIds)
        syncRequestIds(ownerUserId, incomingIds, FriendRequestDirection.Incoming)
        syncRequestIds(ownerUserId, outgoingIds, FriendRequestDirection.Outgoing)

        val relatedIds = (friendIds + incomingIds + outgoingIds).distinct().filterNot(String::isBlank)
        if (relatedIds.isNotEmpty()) {
            val relatedProfiles = fetchProfiles(relatedIds)
            if (relatedProfiles.isNotEmpty()) {
                friendsDao.upsertPublicUsers(relatedProfiles)
            }
        }
    }

    suspend fun searchUsers(ownerUserId: String, query: String): List<SearchUserResult> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.length < MIN_SEARCH_LENGTH) return emptyList()

        val snapshots = firestore.collection(USERS_COLLECTION)
            .whereArrayContains(FIELD_NICKNAME_SEARCH_TERMS, normalizedQuery)
            .limit(SEARCH_RESULT_LIMIT)
            .get()
            .await()

        val friendIds = friendsDao.getFriendIds(ownerUserId).toSet()
        val incomingIds = friendsDao.getRequestIds(ownerUserId, FriendRequestDirection.Incoming.value).toSet()
        val outgoingIds = friendsDao.getRequestIds(ownerUserId, FriendRequestDirection.Outgoing.value).toSet()

        val profiles = snapshots.documents
            .mapNotNull { doc ->
                val userId = doc.id
                if (userId == ownerUserId) {
                    null
                } else {
                    doc.toCachedPublicUser(
                        userId = userId,
                        fallbackNickname = doc.getString(FIELD_NICKNAME).orEmpty(),
                        fallbackLevel = doc.getLong(FIELD_LEVEL)?.toInt() ?: 1
                    )
                }
            }
            .distinctBy { it.userId }
            .sortedBy { it.nickname.lowercase() }

        if (profiles.isNotEmpty()) {
            friendsDao.upsertPublicUsers(profiles)
        }

        return profiles.map { profile ->
            SearchUserResult(
                profile = profile.toPublicUserProfile(),
                relationState = when (profile.userId) {
                    in friendIds -> FriendRelationState.Friends
                    in incomingIds -> FriendRelationState.IncomingRequest
                    in outgoingIds -> FriendRelationState.OutgoingRequest
                    else -> FriendRelationState.None
                }
            )
        }
    }

    suspend fun sendFriendRequest(ownerUserId: String, targetUserId: String) {
        if (ownerUserId.isBlank() || targetUserId.isBlank() || ownerUserId == targetUserId) return

        val batch = firestore.batch()
        val ownerRef = firestore.collection(USERS_COLLECTION).document(ownerUserId)
        val targetRef = firestore.collection(USERS_COLLECTION).document(targetUserId)
        batch.update(ownerRef, FIELD_OUTGOING_REQUESTS, FieldValue.arrayUnion(targetUserId))
        batch.update(targetRef, FIELD_INCOMING_REQUESTS, FieldValue.arrayUnion(ownerUserId))
        batch.commit().await()

        val now = Instant.now()
        friendsDao.upsertRequest(
            FriendRequestEntity(
                ownerUserId = ownerUserId,
                requestUserId = targetUserId,
                direction = FriendRequestDirection.Outgoing.value,
                createdAt = now
            )
        )
    }

    suspend fun acceptFriendRequest(ownerUserId: String, requesterId: String) {
        if (ownerUserId.isBlank() || requesterId.isBlank() || ownerUserId == requesterId) return

        val batch = firestore.batch()
        val ownerRef = firestore.collection(USERS_COLLECTION).document(ownerUserId)
        val requesterRef = firestore.collection(USERS_COLLECTION).document(requesterId)
        batch.update(ownerRef, FIELD_INCOMING_REQUESTS, FieldValue.arrayRemove(requesterId))
        batch.update(ownerRef, FIELD_FRIENDS, FieldValue.arrayUnion(requesterId))
        batch.update(requesterRef, FIELD_OUTGOING_REQUESTS, FieldValue.arrayRemove(ownerUserId))
        batch.update(requesterRef, FIELD_FRIENDS, FieldValue.arrayUnion(ownerUserId))
        batch.commit().await()

        val now = Instant.now()
        friendsDao.deleteRequest(ownerUserId, requesterId, FriendRequestDirection.Incoming.value)
        friendsDao.upsertFriend(FriendEntity(ownerUserId = ownerUserId, friendUserId = requesterId, addedAt = now))
    }

    suspend fun declineFriendRequest(ownerUserId: String, requesterId: String) {
        if (ownerUserId.isBlank() || requesterId.isBlank() || ownerUserId == requesterId) return

        val batch = firestore.batch()
        val ownerRef = firestore.collection(USERS_COLLECTION).document(ownerUserId)
        val requesterRef = firestore.collection(USERS_COLLECTION).document(requesterId)
        batch.update(ownerRef, FIELD_INCOMING_REQUESTS, FieldValue.arrayRemove(requesterId))
        batch.update(requesterRef, FIELD_OUTGOING_REQUESTS, FieldValue.arrayRemove(ownerUserId))
        batch.commit().await()

        friendsDao.deleteRequest(ownerUserId, requesterId, FriendRequestDirection.Incoming.value)
    }

    suspend fun refreshPublicProfile(userId: String) {
        if (userId.isBlank()) return

        val snapshot = firestore.collection(USERS_COLLECTION).document(userId).get().await()
        if (!snapshot.exists()) return

        val fallbackProgress = userProgressDao.getUserById(userId)
        friendsDao.upsertPublicUser(
            snapshot.toCachedPublicUser(
                userId = userId,
                fallbackNickname = fallbackProgress?.nickname.orEmpty(),
                fallbackLevel = fallbackProgress?.level ?: 1
            )
        )
    }

    suspend fun updateAvatarPreset(ownerUserId: String, avatarPresetId: String) {
        if (ownerUserId.isBlank()) return

        val current = friendsDao.getPublicUser(ownerUserId)
        val fallbackProgress = userProgressDao.getUserById(ownerUserId)
        val updated = (current ?: CachedPublicUserEntity(
            userId = ownerUserId,
            nickname = fallbackProgress?.nickname.orEmpty(),
            level = fallbackProgress?.level ?: 1,
            avatarPresetId = avatarPresetId,
            badgeIds = emptyList(),
            updatedAt = Instant.now()
        )).copy(
            avatarPresetId = avatarPresetId,
            updatedAt = Instant.now()
        )

        friendsDao.upsertPublicUser(updated)
        firestore.collection(USERS_COLLECTION)
            .document(ownerUserId)
            .set(
                mapOf(
                    FIELD_AVATAR_PRESET_ID to avatarPresetId,
                    FIELD_UPDATED_AT to updated.toFirestoreValue()
                ),
                SetOptions.merge()
            )
            .await()
    }

    private suspend fun syncFriendIds(ownerUserId: String, friendIds: List<String>) {
        friendsDao.deleteFriendsForOwner(ownerUserId)
        if (friendIds.isNotEmpty()) {
            friendIds.distinct().forEach { friendId ->
                friendsDao.upsertFriend(
                    FriendEntity(
                        ownerUserId = ownerUserId,
                        friendUserId = friendId,
                        addedAt = Instant.now()
                    )
                )
            }
        }
    }

    private suspend fun syncRequestIds(
        ownerUserId: String,
        requestIds: List<String>,
        direction: FriendRequestDirection
    ) {
        friendsDao.deleteRequestsForOwner(ownerUserId, direction.value)
        requestIds.distinct().forEach { requestUserId ->
            friendsDao.upsertRequest(
                FriendRequestEntity(
                    ownerUserId = ownerUserId,
                    requestUserId = requestUserId,
                    direction = direction.value,
                    createdAt = Instant.now()
                )
            )
        }
    }

    private suspend fun fetchProfiles(userIds: List<String>): List<CachedPublicUserEntity> {
        return userIds.chunked(WHERE_IN_LIMIT).flatMap { chunk ->
            val snapshots = firestore.collection(USERS_COLLECTION)
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()

            snapshots.documents.map { doc ->
                doc.toCachedPublicUser(
                    userId = doc.id,
                    fallbackNickname = doc.getString(FIELD_NICKNAME).orEmpty(),
                    fallbackLevel = doc.getLong(FIELD_LEVEL)?.toInt() ?: 1
                )
            }
        }
    }

    private suspend fun ensureRemoteDefaults(
        ownerUserId: String,
        profile: CachedPublicUserEntity,
        friendIds: List<String>,
        incomingIds: List<String>,
        outgoingIds: List<String>
    ) {
        firestore.collection(USERS_COLLECTION)
            .document(ownerUserId)
            .set(
                mapOf(
                    FIELD_NICKNAME to profile.nickname,
                    FIELD_NICKNAME_LOWER to profile.nickname.trim().lowercase(),
                    FIELD_NICKNAME_SEARCH_TERMS to buildNicknameSearchTerms(profile.nickname),
                    FIELD_AVATAR_PRESET_ID to profile.avatarPresetId,
                    FIELD_BADGE_IDS to profile.badgeIds,
                    FIELD_FRIENDS to friendIds,
                    FIELD_INCOMING_REQUESTS to incomingIds,
                    FIELD_OUTGOING_REQUESTS to outgoingIds,
                    FIELD_LEVEL to profile.level,
                    FIELD_UPDATED_AT to profile.toFirestoreValue()
                ),
                SetOptions.merge()
            )
            .await()
    }

    private fun DocumentSnapshot.toCachedPublicUser(
        userId: String,
        fallbackNickname: String,
        fallbackLevel: Int
    ): CachedPublicUserEntity {
        val nickname = getString(FIELD_NICKNAME)?.takeIf { it.isNotBlank() } ?: fallbackNickname
        return CachedPublicUserEntity(
            userId = userId,
            nickname = nickname.ifBlank { "User" },
            level = getLong(FIELD_LEVEL)?.toInt() ?: fallbackLevel,
            avatarPresetId = getString(FIELD_AVATAR_PRESET_ID) ?: DEFAULT_AVATAR_PRESET_ID,
            badgeIds = getStringList(FIELD_BADGE_IDS),
            updatedAt = getTimestamp(FIELD_UPDATED_AT)?.toInstant() ?: Instant.now()
        )
    }

    private fun DocumentSnapshot.getStringList(field: String): List<String> {
        @Suppress("UNCHECKED_CAST")
        return (get(field) as? List<String>).orEmpty()
    }

    private fun CachedPublicUserEntity.toFirestoreValue(): Any = updatedAt ?: Instant.now()

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val FIELD_NICKNAME = "nickname"
        private const val FIELD_LEVEL = "level"
        private const val FIELD_UPDATED_AT = "updatedAt"
        private const val FIELD_FRIENDS = "friends"
        private const val FIELD_INCOMING_REQUESTS = "incomingFriendRequests"
        private const val FIELD_OUTGOING_REQUESTS = "outgoingFriendRequests"
        private const val FIELD_AVATAR_PRESET_ID = "avatarPresetId"
        private const val FIELD_BADGE_IDS = "badgeIds"
        private const val FIELD_NICKNAME_LOWER = "nicknameLower"
        private const val FIELD_NICKNAME_SEARCH_TERMS = "nicknameSearchTerms"
        private const val WHERE_IN_LIMIT = 10
        const val MIN_SEARCH_LENGTH = 2
        private const val SEARCH_RESULT_LIMIT = 20L
    }
}
