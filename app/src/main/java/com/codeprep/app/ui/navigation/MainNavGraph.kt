package com.codeprep.app.ui.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(startDestination = Screen.Home.route, route = "main") {
        composable(Screen.Home.route) { Text("Home") }
        composable(Screen.CourseList.route) { Text("Courses") }
        composable(Screen.LessonDetail.route) { Text("Lesson Detail") }
        composable(Screen.Quiz.route) { Text("Quiz") }
        composable(Screen.AskAI.route) { Text("Ask AI") }
        composable(Screen.Profile.route) { Text("Profile") }
        composable(Screen.FriendSuggestion.route) { Text("Friends") }
    }
}