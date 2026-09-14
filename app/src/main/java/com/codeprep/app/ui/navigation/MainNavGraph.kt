package com.codeprep.app.ui.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.codeprep.app.data.settings.PendingSettingsActionHolder
import com.codeprep.app.ui.friends.AddFriendsScreen
import com.codeprep.app.ui.friends.FriendProfileScreen
import com.codeprep.app.ui.ai.AskAiScreen
import com.codeprep.app.ui.course.CourseListScreen
import com.codeprep.app.ui.home.HomeScreen
import com.codeprep.app.ui.lesson.LessonDetailScreen
import com.codeprep.app.ui.lesson.LessonListScreen
import com.codeprep.app.ui.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.codeprep.app.ui.quiz.QuizScreen

fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    pendingSettingsActionHolder: PendingSettingsActionHolder
) {
    navigation(startDestination = Screen.Home.route, route = "main") {
        composable(Screen.Home.route) {
            HomeScreen(
                onCoursesClick = { navController.navigate(Screen.CourseList.route) }
            )
        }

        composable(Screen.CourseList.route) {
            CourseListScreen(
                onCourseClick = { courseId ->
                    navController.navigate(Screen.LessonList.createRoute(courseId))
                }
            )
        }

        composable(Screen.LessonList.route) {
            LessonListScreen(
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.LessonDetail.createRoute(lessonId))
                }
            )
        }

        composable(
            route = Screen.LessonDetail.route,
            arguments = listOf(
                navArgument("lessonId") { nullable = false },
                navArgument("openAi") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            LessonDetailScreen(
                startWithAiOpen = backStackEntry.arguments?.getBoolean("openAi") == true,
                onStartQuiz = { lessonId ->
                    navController.navigate(Screen.Quiz.createRoute(lessonId))
                },
                onOpenSettings = {
                    openAiSettings(navController, pendingSettingsActionHolder)
                }
            )
        }
        composable(Screen.Quiz.route) {
            QuizScreen(
                onQuizFinished = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.AskAI.route) {
            AskAiScreen(
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.LessonDetail.createRoute(lessonId, openAi = true))
                },
                onOpenSettings = {
                    openAiSettings(navController, pendingSettingsActionHolder)
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onAddFriends = { navController.navigate(Screen.AddFriends.route) },
                onFriendClick = { friendId ->
                    navController.navigate(Screen.FriendProfile.createRoute(friendId))
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                pendingSettingsActionHolder = pendingSettingsActionHolder
            )
        }
        composable(Screen.AddFriends.route) {
            AddFriendsScreen(
                onBack = { navController.popBackStack() },
                onFriendClick = { friendId ->
                    navController.navigate(Screen.FriendProfile.createRoute(friendId))
                }
            )
        }
        composable(
            route = Screen.FriendProfile.route,
            arguments = listOf(navArgument("friendId") { nullable = false })
        ) {
            FriendProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun openAiSettings(
    navController: NavHostController,
    pendingSettingsActionHolder: PendingSettingsActionHolder
) {
    pendingSettingsActionHolder.requestAiSettings()
    navigateToTopLevelRoute(navController, Screen.Profile.route)
}

internal fun navigateToTopLevelRoute(
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
