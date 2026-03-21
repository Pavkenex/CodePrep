package com.codeprep.app.data.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSettingsStoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var store: AppSettingsStore

    @Before
    fun setUp() {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        store = AppSettingsStore(context)
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun feedbackSettings_defaultToEnabledAndPersistUpdates() {
        assertTrue(store.isSoundEffectsEnabled())
        assertTrue(store.isHapticsEnabled())

        store.setSoundEffectsEnabled(false)
        store.setHapticsEnabled(false)

        assertFalse(store.isSoundEffectsEnabled())
        assertFalse(store.isHapticsEnabled())
    }

    @Test
    fun feedbackSettingsFlows_emitUpdatedValues() = runBlocking {
        store.setSoundEffectsEnabled(false)
        store.setHapticsEnabled(false)

        assertFalse(store.soundEffectsEnabled().first())
        assertFalse(store.hapticsEnabled().first())
    }
}
