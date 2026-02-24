package com.codeprep.app.ui.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(startDestination = Screen.Login.route, route = "auth") {
        composable(Screen.Login.route) {
            Button(
                onClick = {
                    navController.navigate("main") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            ) {
                Text("Go to Main")
            }
        }
        composable(Screen.Register.route) {
            Text("Register Screen")
        }
    }
}

