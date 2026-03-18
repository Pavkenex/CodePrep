package com.codeprep.app.ui.secretfact

enum class SecretFactPhase {
    Idle,
    Armed,
    Revealed
}

data class SecretFactState(
    val phase: SecretFactPhase = SecretFactPhase.Idle,
    val isHintVisible: Boolean = false,
    val currentRoute: String? = null,
    val requiresFreshLandscapeRotation: Boolean = false
)

sealed interface SecretFactEvent {
    data class ShakeDetected(
        val route: String,
        val isLandscape: Boolean
    ) : SecretFactEvent

    data class OrientationChanged(
        val isLandscape: Boolean
    ) : SecretFactEvent

    data object TimeoutExpired : SecretFactEvent

    data object Dismissed : SecretFactEvent
}

object SecretFactRoutes {
    private val supportedRoutes = setOf(
        "home",
        "course_list",
        "ask_ai",
        "profile"
    )

    fun isSupported(route: String): Boolean = route in supportedRoutes
}
