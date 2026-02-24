package com.codeprep.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.codeprep.app.ui.navigation.authNavGraph
import com.codeprep.app.ui.navigation.mainNavGraph
import com.codeprep.app.ui.theme.CodePrepTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.firestore.collection("test").add(mapOf("hello" to "world"))
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
    NavHost(navController = navController, startDestination = "auth") {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CodePrepTheme {
        Greeting("Android")
    }
}
