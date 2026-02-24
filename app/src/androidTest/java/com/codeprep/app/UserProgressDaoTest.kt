package com.codeprep.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.codeprep.app.data.local.CodePrepDatabase
import com.codeprep.app.data.local.dao.UserProgressDao
import com.codeprep.app.data.local.entity.UserProgressEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.jvm.Throws


class UserProgressDaoTest {
    private lateinit var db: CodePrepDatabase
    private lateinit var dao: UserProgressDao

    @Before
    fun createDb(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, CodePrepDatabase::class.java).build()
        dao = db.userProgressDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb(){
        db.close()
    }

    @Test
    fun writeUserProgressAndReadInList() = runTest {
        val userStats = UserProgressEntity(
            userId = "test_user_1",
            xp = 150,
            level = 2,
            streak = 5,
            hearts = 3,
            heartsLockedUntil = null, // Može biti null jer je Long?
            lastActiveDate = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // B) AKCIJA (Act)
        dao.upsert(userStats)

        // Pokušavamo da ga učitamo nazad koristeći ISTI ID
        val loadedStats = dao.getProgress("test_user_1")

        // C) PROVERA (Assert)
        // Proveravamo da li smo dobili podatke nazad
        assertEquals(userStats.userId, loadedStats?.userId)
        assertEquals(150, loadedStats?.xp)
        assertEquals(2, loadedStats?.level)
    }
}