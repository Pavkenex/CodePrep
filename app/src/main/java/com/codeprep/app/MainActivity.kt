package com.codeprep.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import com.codeprep.app.feedback.AppFeedback
import com.codeprep.app.feedback.ProvideAppFeedback
import com.codeprep.app.data.settings.PendingSettingsActionHolder
import com.codeprep.app.ui.components.TopBarStats
import com.codeprep.app.ui.navigation.CodePrepBottomBar
import com.codeprep.app.ui.navigation.Screen
import com.codeprep.app.ui.navigation.SessionBootstrapViewModel
import com.codeprep.app.ui.navigation.authNavGraph
import com.codeprep.app.ui.navigation.formatHeartRefillCountdown
import com.codeprep.app.ui.navigation.mainNavGraph
import com.codeprep.app.ui.navigation.navigateToTopLevelRoute
import com.codeprep.app.ui.secretfact.SecretFactSensorController
import com.codeprep.app.ui.secretfact.SecretFactOverlay
import com.codeprep.app.ui.secretfact.SecretFactViewModel
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.CodePrepTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appFeedback: AppFeedback

    @Inject
    lateinit var pendingSettingsActionHolder: PendingSettingsActionHolder

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            CodePrepTheme {
                ProvideAppFeedback(appFeedback = appFeedback) {
                    RootNavGraph(
                        pendingSettingsActionHolder = pendingSettingsActionHolder
                    )
                }
            }
        }

    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun RootNavGraph(
    pendingSettingsActionHolder: PendingSettingsActionHolder
) {
    val navController = rememberNavController()
    val bootstrapViewModel: SessionBootstrapViewModel = hiltViewModel()
    val secretFactViewModel: SecretFactViewModel = hiltViewModel()
    val secretFactUiState by secretFactViewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId by bootstrapViewModel.currentUserId.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) "main" else "auth"
    }
    val context = LocalContext.current
    val currentRouteState = rememberUpdatedState(currentRoute)
    val sensorController = remember(context) {
        SecretFactSensorController(
            context = context.applicationContext,
            onShake = {
                val route = currentRouteState.value ?: return@SecretFactSensorController
                secretFactViewModel.onShake(route = route, isLandscape = false)
            }
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

    LaunchedEffect(currentRoute) {
        secretFactViewModel.onRouteChanged(currentRoute)
    }

    DisposableEffect(sensorController) {
        sensorController.start()
        onDispose {
            sensorController.stop()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                     localizedStringResource(
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
            mainNavGraph(
                navController = navController,
                pendingSettingsActionHolder = pendingSettingsActionHolder
            )
        }

        if (shouldShowBottomNav) {
            CodePrepBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navigateToTopLevelRoute(
                        navController = navController,
                        route = route
                    )
                }
            )
        }
        }

        SecretFactOverlay(
            uiState = secretFactUiState,
            onDismiss = secretFactViewModel::onDismiss
        )
    }
}
