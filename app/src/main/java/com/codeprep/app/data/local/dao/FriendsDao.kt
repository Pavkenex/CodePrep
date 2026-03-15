package com.codeprep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.codeprep.app.data.local.entity.CachedPublicUserEntity
import com.codeprep.app.data.local.entity.FriendEntity
import com.codeprep.app.data.local.entity.FriendRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendsDao {
    @Query(
        """
        SELECT p.* FROM friends f
        INNER JOIN cached_public_users p ON p.userId = f.friendUserId
        WHERE f.ownerUserId = :ownerUserId
        ORDER BY LOWER(p.nickname) ASC
        """
    )
    fun observeFriends(ownerUserId: String): Flow<List<CachedPublicUserEntity>>

    @Query(
        """
        SELECT p.* FROM friend_requests r
        INNER JOIN cached_public_users p ON p.userId = r.requestUserId
        WHERE r.ownerUserId = :ownerUserId AND r.direction = :direction
        ORDER BY LOWER(p.nickname) ASC
        """
    )
    fun observeRequests(ownerUserId: String, direction: String): Flow<List<CachedPublicUserEntity>>

    @Query("SELECT * FROM cached_public_users WHERE userId = :userId")
    fun observePublicUser(userId: String): Flow<CachedPublicUserEntity?>

    @Query("SELECT * FROM cached_public_users WHERE userId = :userId")
    suspend fun getPublicUser(userId: String): CachedPublicUserEntity?

    @Query("SELECT friendUserId FROM friends WHERE ownerUserId = :ownerUserId")
    suspend fun getFriendIds(ownerUserId: String): List<String>

    @Query(
        "SELECT requestUserId FROM friend_requests WHERE ownerUserId = :ownerUserId AND direction = :direction"
    )
    suspend fun getRequestIds(ownerUserId: String, direction: String): List<String>

    @Upsert
    suspend fun upsertPublicUsers(users: List<CachedPublicUserEntity>)

    @Upsert
    suspend fun upsertPublicUser(user: CachedPublicUserEntity)

    @Upsert
    suspend fun upsertFriend(friend: FriendEntity)

    @Upsert
    suspend fun upsertRequest(request: FriendRequestEntity)

    @Query("DELETE FROM friends WHERE ownerUserId = :ownerUserId")
    suspend fun deleteFriendsForOwner(ownerUserId: String)

    @Query("DELETE FROM friend_requests WHERE ownerUserId = :ownerUserId AND direction = :direction")
    suspend fun deleteRequestsForOwner(ownerUserId: String, direction: String)

    @Query(
        "DELETE FROM friend_requests WHERE ownerUserId = :ownerUserId AND requestUserId = :requestUserId AND direction = :direction"
    )
    suspend fun deleteRequest(ownerUserId: String, requestUserId: String, direction: String)

    @Query("DELETE FROM friends WHERE ownerUserId = :ownerUserId AND friendUserId = :friendUserId")
    suspend fun deleteFriend(ownerUserId: String, friendUserId: String)
}
