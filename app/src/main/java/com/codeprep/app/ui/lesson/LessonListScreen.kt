package com.codeprep.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codeprep.app.ui.components.LessonPathNode
import com.codeprep.app.ui.components.NodeState
import com.codeprep.app.ui.theme.*

@Composable
fun LessonListScreen(
    onLessonClick: (String) -> Unit,
    viewModel: LessonListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lessons = state.lessons

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (lessons.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No lessons available",
                    color = TextLight,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                lessons.forEachIndexed { index, lessonItem ->
                    val offsetX = when (index % 4) {
                        0 -> 0.dp
                        1 -> (-60).dp
                        2 -> 0.dp
                        3 -> 60.dp
                        else -> 0.dp
                    }

                    val nextOffsetX = if (index < lessons.lastIndex) {
                        when ((index + 1) % 4) {
                            0 -> 0.dp
                            1 -> (-60).dp
                            2 -> 0.dp
                            3 -> 60.dp
                            else -> 0.dp
                        }
                    } else 0.dp

                    // The Node
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LessonPathNode(
                            state = when {
                                lessonItem.isPerfect -> NodeState.PERFECT
                                lessonItem.isCompleted -> NodeState.COMPLETED
                                lessonItem.isUnlocked && !lessonItem.isBlockedByHearts -> NodeState.ACTIVE
                                else -> NodeState.LOCKED
                            },
                            onClick = { 
                                if (lessonItem.canOpen) onLessonClick(lessonItem.lesson.lessonId) 
                            },
                            modifier = Modifier.offset(x = offsetX)
                        )
                    }

                    // The Path Connector to Next Node
                    if (index < lessons.lastIndex) {
                        val pathColor = if (lessonItem.isCompleted) SunYellow else LockedGrey
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width / 2
                                val start = Offset(centerX + offsetX.toPx(), 0f)
                                val end = Offset(centerX + nextOffsetX.toPx(), size.height)
                                
                                drawLine(
                                    color = pathColor,
                                    start = start,
                                    end = end,
                                    strokeWidth = 12.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
            }
        }
    }
}
