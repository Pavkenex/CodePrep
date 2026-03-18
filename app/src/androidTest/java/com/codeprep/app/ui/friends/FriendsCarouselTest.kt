package com.codeprep.app.ui.friends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.CodePrepTheme
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FriendsCarouselTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun friendCard_centersContentWithinCard() {
        val friend = FriendListItemUiModel(
            userId = "friend-1",
            nickname = "Pavke",
            level = 1,
            avatarPresetId = AvatarPresets.first().id,
            badgeIds = emptyList()
        )

        composeTestRule.setContent {
            CodePrepTheme {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                ) {
                    FriendsCarousel(
                        friends = listOf(friend),
                        onFriendClick = {}
                    )
                }
            }
        }

        val cardBounds = composeTestRule
            .onNodeWithTag("friend-card-${friend.userId}")
            .fetchSemanticsNode()
            .boundsInRoot
        val initialBounds = composeTestRule
            .onNodeWithText("P", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot

        val cardCenter = (cardBounds.left + cardBounds.right) / 2f
        val initialCenter = (initialBounds.left + initialBounds.right) / 2f
        val maxOffset = with(composeTestRule.density) { 12.dp.toPx() }

        assertTrue(
            "Expected friend card content to be centered, but offset was ${abs(initialCenter - cardCenter)} px",
            abs(initialCenter - cardCenter) <= maxOffset
        )
    }
}
