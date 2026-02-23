package com.example.codeprep

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
import com.example.codeprep.ui.navigation.authNavGraph
import com.example.codeprep.ui.navigation.mainNavGraph
import com.example.codeprep.ui.theme.CodePrepTheme
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
