package com.codeprep.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codeprep.app.ui.navigation.Screen
import com.codeprep.app.ui.navigation.SessionBootstrapViewModel
import com.codeprep.app.ui.navigation.authNavGraph
import com.codeprep.app.ui.navigation.mainNavGraph
import com.codeprep.app.ui.theme.CodePrepTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodePrepTheme {
                RootNavGraph()
            }
        }

    }
}

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val bootstrapViewModel: SessionBootstrapViewModel = hiltViewModel()
    val currentUserId by bootstrapViewModel.currentUserId.collectAsStateWithLifecycle()
    val currentUserProgress by bootstrapViewModel.currentUserProgress.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) "main" else "auth"
    }
    val bottomNavItems = remember {
        listOf(
            BottomNavItem("Početna", Screen.Home.route, Icons.Default.Home),
            BottomNavItem("Kursevi", Screen.CourseList.route, Icons.Default.School),
            BottomNavItem("Pitaj AI", Screen.AskAI.route, Icons.Default.Psychology),
            BottomNavItem("Profil", Screen.Profile.route, Icons.Default.Person)
        )
    }
    val mainRoutes = remember {
        setOf(
            Screen.Home.route,
            Screen.CourseList.route,
            Screen.LessonList.route,
            Screen.LessonDetail.route,
            Screen.Quiz.route,
            Screen.AskAI.route,
            Screen.Profile.route,
            Screen.FriendSuggestion.route
        )
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            bootstrapViewModel.refreshHeartsOnSessionStart()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val shouldShowSessionBanner = currentUserId != null && currentRoute !in setOf(
            Screen.Login.route,
            Screen.Register.route
        )
        val shouldShowBottomNav = currentRoute in mainRoutes

        if (shouldShowSessionBanner) {
            val currentAuthUser = FirebaseAuth.getInstance().currentUser
            val resolvedNickname = currentUserProgress?.nickname?.takeIf { it.isNotBlank() }
                ?: currentAuthUser?.displayName?.takeIf { it.isNotBlank() }
                ?: currentAuthUser?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
                ?: "Korisnik"
            SessionDebugBanner(
                nickname = resolvedNickname,
                hearts = currentUserProgress?.hearts ?: 5,
                streak = currentUserProgress?.streak ?: 0
            )
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.weight(1f)
        ) {
            authNavGraph(navController)
            mainNavGraph(navController)
        }

        if (shouldShowBottomNav) {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = isBottomItemSelected(
                        itemRoute = item.route,
                        currentRoute = currentRoute
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo("main") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    }
}

private fun isBottomItemSelected(itemRoute: String, currentRoute: String?): Boolean {
    return when (itemRoute) {
        Screen.CourseList.route -> currentRoute in setOf(
            Screen.CourseList.route,
            Screen.LessonList.route,
            Screen.LessonDetail.route,
            Screen.Quiz.route
        )

        Screen.Profile.route -> currentRoute in setOf(
            Screen.Profile.route,
            Screen.FriendSuggestion.route
        )

        else -> currentRoute == itemRoute
    }
}

private data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@Composable
private fun SessionDebugBanner(
    nickname: String,
    hearts: Int,
    streak: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "username: $nickname",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "hearts: $hearts",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "streak: $streak",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
