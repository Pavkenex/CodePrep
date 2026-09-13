package com.codeprep.app.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.R
import com.codeprep.app.ui.localization.localizedPluralStringResource
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.CardinalRed
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.DeepCharcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight
import com.codeprep.app.ui.theme.TrueBlack

internal data class CourseListLayoutSpec(
    val useTabletContainer: Boolean,
    val screenPaddingDp: Int,
    val verticalSpacingDp: Int,
    val titleTopPaddingDp: Int,
    val titleBottomPaddingDp: Int,
    val subtitleBottomPaddingDp: Int,
    val maxContainerWidthDp: Int,
    val cardPaddingDp: Int,
    val cardSectionSpacingDp: Int,
    val pillHorizontalSpacingDp: Int,
    val pillVerticalSpacingDp: Int
)

internal const val COURSE_LIST_ROOT_TAG = "course-list-root"
internal const val COURSE_LIST_CONTAINER_TAG = "course-list-container"
internal const val COURSE_LIST_TITLE_TAG = "course-list-title"
internal const val COURSE_LIST_SUBTITLE_TAG = "course-list-subtitle"

private const val COMPACT_HEIGHT_THRESHOLD_DP = 480

internal fun courseListLayoutFor(
    screenWidthDp: Int,
    screenHeightDp: Int = Int.MAX_VALUE
): CourseListLayoutSpec {
    val isTabletWidth = screenWidthDp >= 600
    val isCompactHeight = screenHeightDp < COMPACT_HEIGHT_THRESHOLD_DP

    return when {
        !isTabletWidth -> CourseListLayoutSpec(
            useTabletContainer = false,
            screenPaddingDp = 16,
            verticalSpacingDp = 16,
            titleTopPaddingDp = 24,
            titleBottomPaddingDp = 8,
            subtitleBottomPaddingDp = 20,
            maxContainerWidthDp = 0,
            cardPaddingDp = 18,
            cardSectionSpacingDp = 14,
            pillHorizontalSpacingDp = 8,
            pillVerticalSpacingDp = 8
        )

        isCompactHeight -> CourseListLayoutSpec(
            useTabletContainer = true,
            screenPaddingDp = 24,
            verticalSpacingDp = 12,
            titleTopPaddingDp = 8,
            titleBottomPaddingDp = 4,
            subtitleBottomPaddingDp = 8,
            maxContainerWidthDp = 800,
            cardPaddingDp = 16,
            cardSectionSpacingDp = 12,
            pillHorizontalSpacingDp = 8,
            pillVerticalSpacingDp = 8
        )

        else -> CourseListLayoutSpec(
            useTabletContainer = true,
            screenPaddingDp = 40,
            verticalSpacingDp = 24,
            titleTopPaddingDp = 40,
            titleBottomPaddingDp = 16,
            subtitleBottomPaddingDp = 32,
            maxContainerWidthDp = 800,
            cardPaddingDp = 22,
            cardSectionSpacingDp = 18,
            pillHorizontalSpacingDp = 10,
            pillVerticalSpacingDp = 10
        )
    }
}

@Composable
fun CourseListScreen(
    viewModel: CourseViewModel = hiltViewModel(),
    onCourseClick: (String) -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val configuration = LocalConfiguration.current
    val layout = courseListLayoutFor(
        screenWidthDp = configuration.screenWidthDp,
        screenHeightDp = configuration.screenHeightDp
    )

    CourseListScreenContent(
        courses = courses,
        layout = layout,
        onCourseClick = onCourseClick
    )
}

@Composable
internal fun CourseListScreenContent(
    courses: List<ModuleCardUi>,
    layout: CourseListLayoutSpec,
    onCourseClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag(COURSE_LIST_ROOT_TAG)
    ) {
        if (layout.useTabletContainer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = layout.screenPaddingDp.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CourseListContentList(
                    courses = courses,
                    layout = layout,
                    onCourseClick = onCourseClick,
                    modifier = Modifier
                        .width(layout.maxContainerWidthDp.dp)
                        .fillMaxHeight()
                        .testTag(COURSE_LIST_CONTAINER_TAG)
                )
            }
        } else {
            CourseListContentList(
                courses = courses,
                layout = layout,
                onCourseClick = onCourseClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = layout.screenPaddingDp.dp)
                    .testTag(COURSE_LIST_CONTAINER_TAG)
            )
        }
    }
}

@Composable
private fun CourseListContentList(
    courses: List<ModuleCardUi>,
    layout: CourseListLayoutSpec,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(layout.verticalSpacingDp.dp)
    ) {
        item(key = "course-list-title") {
            Text(
                text = localizedStringResource(R.string.course_list_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = IceWhite
                ),
                modifier = Modifier.padding(
                    top = layout.titleTopPaddingDp.dp,
                    bottom = layout.titleBottomPaddingDp.dp
                ).testTag(COURSE_LIST_TITLE_TAG)
            )
        }
        item(key = "course-list-subtitle") {
            Text(
                text = localizedStringResource(R.string.course_list_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                modifier = Modifier
                    .padding(bottom = layout.subtitleBottomPaddingDp.dp)
                    .testTag(COURSE_LIST_SUBTITLE_TAG)
            )
        }

        if (courses.isEmpty()) {
            item(key = "course-list-empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight(0.6f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = localizedStringResource(R.string.course_list_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                }
            }
        } else {
            items(courses, key = { it.courseId }) { course ->
                ModuleJourneyCard(
                    module = course,
                    layout = layout,
                    onClick = { if (!course.isLocked) onCourseClick(course.courseId) }
                )
            }
            item(key = "course-list-bottom-spacer") { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ModuleJourneyCard(
    module: ModuleCardUi,
    layout: CourseListLayoutSpec,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val completionRatio = if (module.lessonCount == 0) 0f
    else module.completedLessons.toFloat() / module.lessonCount.toFloat()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                enabled = !module.isLocked,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = Charcoal,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (module.isLocked) LockedGrey.copy(alpha = 0.25f) else ElectricCyan.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(layout.cardPaddingDp.dp),
            verticalArrangement = Arrangement.spacedBy(layout.cardSectionSpacingDp.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModuleIconBubble(icon = module.icon, isLocked = module.isLocked)
                Spacer(modifier = Modifier.weight(1f))
                if (module.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = localizedStringResource(R.string.course_content_locked),
                        tint = LockedGrey
                    )
                } else if (module.perfectLessons == module.lessonCount && module.lessonCount > 0) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = localizedStringResource(R.string.course_content_perfect),
                        tint = SunYellow
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = localizedStringResource(R.string.course_content_continue),
                        tint = ElectricCyan
                    )
                }
            }

            Text(
                text = module.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = IceWhite
                )
            )
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            ModuleJourneyCardPills(
                pills = listOf(
                    CourseCardPill(
                        text = localizedPluralStringResource(
                            R.plurals.course_pill_lessons,
                            module.lessonCount,
                            module.lessonCount
                        ),
                        accent = ElectricCyan
                    ),
                    CourseCardPill(
                        text = localizedPluralStringResource(
                            R.plurals.course_pill_stars,
                            module.perfectLessons,
                            module.perfectLessons
                        ),
                        accent = SunYellow
                    ),
                    CourseCardPill(
                        text = if (module.isLocked) {
                            localizedStringResource(R.string.course_progress_locked)
                        } else {
                            localizedStringResource(R.string.course_progress_cleared, module.completedLessons)
                        },
                        accent = if (module.isLocked) CardinalRed else ElectricCyan
                    )
                ),
                layout = layout
            )

            LinearProgressIndicator(
                progress = { completionRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = ElectricCyan,
                trackColor = DeepCharcoal
            )

            Text(
                text = when {
                    module.isLocked -> localizedStringResource(R.string.course_status_locked)
                    module.continueLessonTitle != null -> localizedStringResource(
                        R.string.course_status_continue,
                        module.continueLessonTitle
                    )
                    else -> localizedStringResource(R.string.course_status_ready)
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (module.isLocked) LockedGrey else ElectricCyan
                )
            )
        }
    }
}

@Composable
private fun ModuleIconBubble(
    icon: String,
    isLocked: Boolean
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isLocked) DeepCharcoal else ElectricCyan),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon.takeIf { it.isNotBlank() }?.take(2) ?: "M",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = if (isLocked) LockedGrey else TrueBlack
        )
    }
}

@Composable
private fun Pill(
    text: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.3f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

internal data class CourseCardPill(
    val text: String,
    val accent: androidx.compose.ui.graphics.Color
)

@Composable
internal fun ModuleJourneyCardPills(
    pills: List<CourseCardPill>,
    layout: CourseListLayoutSpec,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(layout.pillHorizontalSpacingDp.dp),
        verticalArrangement = Arrangement.spacedBy(layout.pillVerticalSpacingDp.dp)
    ) {
        pills.forEach { pill ->
            Pill(
                text = pill.text,
                accent = pill.accent
            )
        }
    }
}
