package com.codeprep.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.codeprep.app.data.local.entity.Question
import com.codeprep.app.ui.theme.CodePrepTheme
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTabletLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tabletLayout_rendersSupportingRailAndPrimaryContentSeparately() {
        composeTestRule.setContent {
            CodePrepTheme {
                Box(
                    modifier = Modifier
                        .width(600.dp)
                        .height(900.dp)
                ) {
                    HomeScreenContent(
                        uiState = HomeUiState(
                            nickname = "test",
                            streak = 4,
                            level = 1,
                            currentLevelXp = 85,
                            xpRequiredForNextLevel = 500,
                            dailyChallenge = DailyChallengeUiState(
                                question = Question(
                                    id = "daily-1",
                                    title = "Question title",
                                    text = "What does this code print?",
                                    options = listOf("0", "1", "2"),
                                    correctIndex = 1,
                                    explanation = "Because the value increments once.",
                                    type = "debugging",
                                    difficulty = "medium",
                                    codeSnippetLanguage = "kotlin",
                                    codeSnippet = "val count = 0\nprintln(count + 1)"
                                ),
                                isLoading = false,
                                isExpanded = true
                            ),
                        ),
                        layout = homeScreenLayoutFor(screenWidthDp = 600),
                        onExpand = {},
                        onAnswer = {},
                        onComplete = {},
                        onCoursesClick = {}
                    )
                }
            }
        }

        val railNode = composeTestRule.onNodeWithTag("home-supporting-rail").assertExists()
        val primaryNode = composeTestRule.onNodeWithTag("home-primary-content").assertExists()
        val launchButtonNode = composeTestRule.onNodeWithTag("home-launch-modules-button").assertExists()

        val railBounds = railNode.fetchSemanticsNode().boundsInRoot
        val primaryBounds = primaryNode.fetchSemanticsNode().boundsInRoot
        val launchButtonBounds = launchButtonNode.fetchSemanticsNode().boundsInRoot
        val maxButtonWidth = with(composeTestRule.density) { 220.dp.toPx() }
        val expectedRailWidth = with(composeTestRule.density) { 230.dp.toPx() }
        val railWidth = railBounds.right - railBounds.left
        val primaryWidth = primaryBounds.right - primaryBounds.left
        val buttonWidth = launchButtonBounds.right - launchButtonBounds.left
        val tolerance = with(composeTestRule.density) { 2.dp.toPx() }
        val railCenter = (railBounds.left + railBounds.right) / 2f
        val buttonCenter = (launchButtonBounds.left + launchButtonBounds.right) / 2f

        assertTrue(
            "Expected supporting rail to stay narrower than primary content on tablet.",
            railWidth < primaryWidth
        )
        assertTrue(
            "Expected supporting rail to widen slightly on tablet, but was $railWidth px.",
            abs(railWidth - expectedRailWidth) <= tolerance
        )
        assertTrue(
            "Expected launch button width to stay capped near 220dp, but was $buttonWidth px.",
            buttonWidth <= maxButtonWidth + tolerance
        )
        assertTrue(
            "Expected launch button width to closely match the 220dp tablet cap.",
            abs(buttonWidth - maxButtonWidth) <= tolerance
        )
        assertTrue(
            "Expected launch button to be centered under the supporting rail.",
            abs(buttonCenter - railCenter) <= tolerance
        )
    }
}
