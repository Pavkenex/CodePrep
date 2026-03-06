package com.codeprep.app.ui.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.codeprep.app.ui.course.CourseListScreen
import com.codeprep.app.ui.home.HomeScreen
import com.codeprep.app.ui.lesson.LessonDetailScreen
import com.codeprep.app.ui.lesson.LessonListScreen
import com.codeprep.app.ui.quiz.QuizScreen

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
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

        composable(Screen.LessonDetail.route) {
            LessonDetailScreen(
                onStartQuiz = { lessonId ->
                    navController.navigate(Screen.Quiz.createRoute(lessonId))
                },
                onAskAI = { _, _, _ ->
                    navController.navigate(Screen.AskAI.route)
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
        composable(Screen.AskAI.route) { Text("Ask AI") }
        composable(Screen.Profile.route) { Text("Profile") }
        composable(Screen.FriendSuggestion.route) { Text("Friends") }
    }
}