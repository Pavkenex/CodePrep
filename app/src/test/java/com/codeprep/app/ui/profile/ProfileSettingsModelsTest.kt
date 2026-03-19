package com.codeprep.app.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileSettingsModelsTest {

    @Test
    fun toggleSettingsSection_opensRequestedSectionWhenNothingIsExpanded() {
        val expanded = toggleSettingsSection(
            current = null,
            requested = ProfileSettingsSection.Language
        )

        assertEquals(ProfileSettingsSection.Language, expanded)
    }

    @Test
    fun toggleSettingsSection_closesRequestedSectionWhenItIsAlreadyExpanded() {
        val expanded = toggleSettingsSection(
            current = ProfileSettingsSection.Conversations,
            requested = ProfileSettingsSection.Conversations
        )

        assertEquals(null, expanded)
    }

    @Test
    fun toggleSettingsSection_keepsOnlyOneSectionExpandedAtATime() {
        val expanded = toggleSettingsSection(
            current = ProfileSettingsSection.Language,
            requested = ProfileSettingsSection.Conversations
        )

        assertEquals(ProfileSettingsSection.Conversations, expanded)
    }
}
