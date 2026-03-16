package com.codeprep.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codeprep.app.ui.components.TopBarStats
import com.codeprep.app.ui.navigation.Screen
import com.codeprep.app.ui.navigation.SessionBootstrapViewModel
import com.codeprep.app.ui.navigation.authNavGraph
import com.codeprep.app.ui.navigation.formatHeartRefillCountdown
import com.codeprep.app.ui.navigation.mainNavGraph
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.CodePrepTheme
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.LockedGrey
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) "main" else "auth"
    }
    
    // Bottom Nav Items - Added Home/Dashboard
    val bottomNavItems = remember {
        listOf(
            BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
            BottomNavItem("Modules", Screen.CourseList.route, Icons.Default.School),
            BottomNavItem("AI Coach", Screen.AskAI.route, Icons.Default.Psychology),
            BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person)
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
            Screen.AddFriends.route,
            Screen.FriendProfile.route
        )
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            bootstrapViewModel.refreshHeartsOnSessionStart()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val shouldShowSessionBanner = currentUserId != null && currentRoute !in setOf(
            Screen.Login.route,
            Screen.Register.route
        )
        val currentUserProgress = if (shouldShowSessionBanner) {
            bootstrapViewModel.currentUserProgress.collectAsStateWithLifecycle().value
        } else {
            null
        }
        val heartRefillCountdown = if (shouldShowSessionBanner) {
            bootstrapViewModel.heartRefillCountdown.collectAsStateWithLifecycle().value
        } else {
            null
        }
        val shouldShowBottomNav = currentRoute in mainRoutes

        if (shouldShowSessionBanner) {
             TopBarStats(
                 hearts = currentUserProgress?.hearts ?: 5,
                 streak = currentUserProgress?.streak ?: 0,
                 heartTimerText = heartRefillCountdown?.let { countdown ->
                     stringResource(
                         R.string.heart_refill_countdown,
                         formatHeartRefillCountdown(countdown)
                     )
                 },
                 modifier = Modifier.statusBarsPadding()
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
            NavigationBar(
                containerColor = AppBackground,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { item ->
                    val selected = isBottomItemSelected(
                        itemRoute = item.route,
                        currentRoute = currentRoute
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navigateToTopLevelRoute(
                                    navController = navController,
                                    route = item.route
                                )
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { 
                            Text(
                                item.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
    }
}

private fun navigateToTopLevelRoute(
    navController: NavHostController,
    route: String
) {
    val popped = navController.popBackStack(route, inclusive = false)
    val currentRoute = navController.currentDestination?.route
    if (popped && currentRoute == route) return

    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun isBottomItemSelected(itemRoute: String, currentRoute: String?): Boolean {
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
    val label: String,
    val route: String,
    val icon: ImageVector
)
