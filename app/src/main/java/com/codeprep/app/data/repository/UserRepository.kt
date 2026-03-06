package com.codeprep.app.data.repository

import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.UserProgressEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserProgressDao,
    private val firestore: FirebaseFirestore
) {

    //Za UI
    fun getUserProgress(userId: String): Flow<UserProgressEntity?> {
        return userDao.getUserProgressStream(userId)
    }

    // 2. HELPER FUNKCIJA ZA AŽURIRANJE
    suspend fun updateProgress(
        userId: String,
        transform: (UserProgressEntity) -> UserProgressEntity
    ) {

        val current = userDao.getUserById(userId) ?: return

        val updated = transform(current).copy(
            updatedAt = System.currentTimeMillis()
        )

        userDao.upsert(updated)

        // KORAK D: (Opciono) Sync sa Firestore
        // firestore.collection("users").document(userId).set(updated)
    }


    suspend fun addXp(userId: String, amount: Int) {
        updateProgress(userId) { progress ->
            val newXp = progress.xp + amount
            // Logika za level up
            progress.copy(
                xp = newXp,
                level = (newXp / 500) + 1
            )
        }
    }

    suspend fun loseHeart(userId: String) {
        updateProgress(userId) { progress ->
            val newHearts = (progress.hearts - 1).coerceAtLeast(0)

            progress.copy(
                hearts = newHearts,
                heartsLockedUntil = if (newHearts == 0 && progress.hearts > 0)
                    System.currentTimeMillis() + 30 * 60 * 1000L // 30 min tajmer samo ako smo upravo pali na 0
                else
                    progress.heartsLockedUntil
            )
        }
    }
}