package com.codeprep.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.codeprep.app.R
import com.codeprep.app.feedback.FeedbackEvent
import com.codeprep.app.feedback.LocalAppFeedback
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.LockedGrey

@Composable
fun CodePrepBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val feedback = LocalAppFeedback.current
    val bottomNavItems = remember {
        listOf(
            BottomNavItem(R.string.nav_home, Screen.Home.route, Icons.Default.Home),
            BottomNavItem(R.string.nav_modules, Screen.CourseList.route, Icons.Default.School),
            BottomNavItem(R.string.nav_ai_coach, Screen.AskAI.route, Icons.Default.Psychology),
            BottomNavItem(R.string.nav_profile, Screen.Profile.route, Icons.Default.Person)
        )
    }

    NavigationBar(
        containerColor = AppBackground,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val label = localizedStringResource(item.labelResId)
            val selected = isBottomBarItemSelected(
                itemRoute = item.route,
                currentRoute = currentRoute
            )
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        feedback.emit(FeedbackEvent.TapPrimary)
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricCyan,
                    selectedTextColor = ElectricCyan,
                    indicatorColor = Charcoal,
                    unselectedIconColor = LockedGrey,
                    unselectedTextColor = LockedGrey
                )
            )
        }
    }
}

private fun isBottomBarItemSelected(itemRoute: String, currentRoute: String?): Boolean {
    return when (itemRoute) {
        Screen.Home.route -> currentRoute == Screen.Home.route

        Screen.CourseList.route -> currentRoute in setOf(
            Screen.CourseList.route,
            Screen.LessonList.route,
            Screen.LessonDetail.route,
            Screen.Quiz.route
        )

        Screen.Profile.route -> currentRoute in setOf(
            Screen.Profile.route,
            Screen.AddFriends.route,
            Screen.FriendProfile.route
        )

        else -> currentRoute == itemRoute
    }
}

private data class BottomNavItem(
    @StringRes val labelResId: Int,
    val route: String,
    val icon: ImageVector
)
