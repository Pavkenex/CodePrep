package com.codeprep.app.ui.friends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.CodePrepTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RequestCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun requestCard_keepsNicknameReadableWithinPhoneWidth() {
        val request = FriendListItemUiModel(
            userId = "requester",
            nickname = "testuser",
            level = 12,
            avatarPresetId = AvatarPresets.first().id,
            badgeIds = emptyList()
        )

        composeTestRule.setContent {
            CodePrepTheme {
                Box(modifier = androidx.compose.ui.Modifier.width(320.dp)) {
                    RequestCard(
                        request = request,
                        isBusy = false,
                        onAccept = {},
                        onDecline = {},
                        onOpenProfile = {}
                    )
                }
            }
        }

        val nicknameWidth = composeTestRule
            .onNodeWithText(request.nickname, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .width

        val minimumReadableWidth = with(composeTestRule.density) { 48.dp.toPx() }
        assertTrue(
            "Expected nickname width to stay readable, but was $nicknameWidth px",
            nicknameWidth >= minimumReadableWidth
        )
    }
}
