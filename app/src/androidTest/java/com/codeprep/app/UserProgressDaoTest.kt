package com.codeprep.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.codeprep.app.data.local.CodePrepDatabase
import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.UserProgressEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant

class UserProgressDaoTest {
    private lateinit var db: CodePrepDatabase
    private lateinit var dao: UserProgressDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, CodePrepDatabase::class.java).build()
        dao = db.userProgressDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserProgressAndReadById() = runTest {
        val userStats = UserProgressEntity(
            userId = "test_user_1",
            nickname = "Pavke",
            xp = 150,
            level = 2,
            streak = 5,
            hearts = 3,
            lastHeartLostAt = Instant.parse("2026-03-16T10:00:00Z"),
            lastActiveDate = Instant.parse("2026-03-16T09:00:00Z"),
            updatedAt = Instant.parse("2026-03-16T10:00:00Z")
        )

        dao.upsert(userStats)
        val loadedStats = dao.getUserById("test_user_1")

        assertEquals(userStats.userId, loadedStats?.userId)
        assertEquals(150, loadedStats?.xp)
        assertEquals(2, loadedStats?.level)
        assertEquals(3, loadedStats?.hearts)
    }
}
