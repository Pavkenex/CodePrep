package com.example.codeprep.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    // Main
    object Home : Screen("home")
    object CourseList : Screen("course_list")
    object LessonDetail : Screen("lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }
    object Quiz : Screen("quiz/{lessonId}") {
        fun createRoute(lessonId: String) = "quiz/$lessonId"
    }
    object AskAI : Screen("ask_ai")
    object Profile : Screen("profile")
    object FriendSuggestion : Screen("friend_suggestion")
}