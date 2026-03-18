package com.codeprep.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.LockedGrey

@Composable
internal fun LessonConnectorPath(
    startOffsetX: Dp,
    endOffsetX: Dp,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(176.dp)
    ) {
        val centerX = size.width / 2
        val startX = centerX + startOffsetX.toPx()
        val endX = centerX + endOffsetX.toPx()
        val startY = 16.dp.toPx()
        val endY = size.height
        val path = Path().apply {
            moveTo(startX, startY)
            cubicTo(
                startX,
                startY + size.height * 0.35f,
                endX,
                endY - size.height * 0.35f,
                endX,
                endY
            )
        }

        drawPath(
            path = path,
            color = if (isCompleted) ElectricCyan else LockedGrey,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

internal fun nodeOffsetForIndex(index: Int) = when (index % 4) {
    0 -> 0.dp
    1 -> (-56).dp
    2 -> 0.dp
    else -> 56.dp
}

