package com.codeprep.app.ui.profile

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.codeprep.app.ui.theme.CodePrepTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FeedbackSettingsSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun feedbackSettingsSection_rendersCurrentStateAndInvokesCallbacks() {
        var soundToggleCount = 0
        var hapticsToggleCount = 0

        composeTestRule.setContent {
            CodePrepTheme {
                FeedbackSettingsSection(
                    soundEffectsEnabled = true,
                    hapticsEnabled = false,
                    onSoundEffectsToggle = { soundToggleCount++ },
                    onHapticsToggle = { hapticsToggleCount++ }
                )
            }
        }

        composeTestRule.onNodeWithTag("feedback_sound_option").assertIsOn()
        composeTestRule.onNodeWithTag("feedback_haptics_option").assertIsOff()

        composeTestRule.onNodeWithText("Sound effects").performClick()
        composeTestRule.onNodeWithText("Haptics").performClick()

        assertEquals(1, soundToggleCount)
        assertEquals(1, hapticsToggleCount)
    }
}
