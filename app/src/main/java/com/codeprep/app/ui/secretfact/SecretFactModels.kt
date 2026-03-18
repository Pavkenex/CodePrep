package com.codeprep.app.ui.secretfact

enum class SecretFactPhase {
    Idle,
    Signaling,
    Revealed
}

data class SecretFactState(
    val phase: SecretFactPhase = SecretFactPhase.Idle,
    val currentRoute: String? = null
)

data class SecretFactUiState(
    val phase: SecretFactPhase = SecretFactPhase.Idle,
    val currentRoute: String? = null,
    val title: String = "",
    val factText: String = "",
    val categoryLabel: String = "",
    val languageCode: String = "en",
    val revealNonce: Long = 0L
)

sealed interface SecretFactEvent {
    data class ShakeDetected(
        val route: String,
        val isLandscape: Boolean
    ) : SecretFactEvent

    data class OrientationChanged(
        val isLandscape: Boolean
    ) : SecretFactEvent

    data class FactResolved(
        val hasFact: Boolean
    ) : SecretFactEvent

    data class RouteChanged(
        val route: String?
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
