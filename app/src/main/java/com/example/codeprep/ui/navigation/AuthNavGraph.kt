package com.example.codeprep.ui.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

class AuthNavGraph {
    fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
        navigation(startDestination = Screen.Login.route, route = "auth") {
            composable(Screen.Login.route) {
                // Placeholder
                Text("Login Screen")
            }
            composable(Screen.Register.route) {
                Text("Register Screen")
            }
        }
    }
}