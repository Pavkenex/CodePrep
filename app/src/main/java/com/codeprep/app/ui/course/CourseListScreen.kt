package com.codeprep.app.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

@Composable
fun CourseListScreen(
    viewModel: CourseViewModel = hiltViewModel(),
    onCourseClick: (String) -> Unit
) {
    val courses by viewModel.courses.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = localizedStringResource(R.string.course_list_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = IceWhite
            ),
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )
        Text(
            text = localizedStringResource(R.string.course_list_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextLight,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (courses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = localizedStringResource(R.string.course_list_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextLight
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(courses, key = { it.courseId }) { course ->
                    ModuleJourneyCard(
                        module = course,
                        onClick = { if (!course.isLocked) onCourseClick(course.courseId) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ModuleJourneyCard(
    module: ModuleCardUi,
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Pill(
                    text = localizedPluralStringResource(
                        R.plurals.course_pill_lessons,
                        module.lessonCount,
                        module.lessonCount
                    ),
                    accent = ElectricCyan
                )
                Pill(
                    text = localizedPluralStringResource(
                        R.plurals.course_pill_stars,
                        module.perfectLessons,
                        module.perfectLessons
                    ),
                    accent = SunYellow
                )
                Pill(
                    text = if (module.isLocked) {
                        localizedStringResource(R.string.course_progress_locked)
                    } else {
                        localizedStringResource(R.string.course_progress_cleared, module.completedLessons)
                    },
                    accent = if (module.isLocked) CardinalRed else ElectricCyan
                )
            }

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
            color = accent
        )
    }
}
