package com.codeprep.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.*

@Composable
fun TopBarStats(
    hearts: Int = 5,
    streak: Int = 0,
    heartTimerText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hearts
        StatBadge(
            icon = Icons.Default.Favorite,
            value = hearts.toString(),
            color = CardinalRed,
            detail = heartTimerText
        )
        
        // Streak
        StatBadge(
            icon = Icons.Default.LocalFireDepartment,
            value = streak.toString(),
            color = SunYellow
        )
    }
}

@Composable
fun StatBadge(
    icon: ImageVector,
    value: String,
    color: Color,
    detail: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                style = AppCodeTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = AppCodeTypography.labelSmall.copy(
                        color = color
                    )
                )
            }
        }
    }
}
