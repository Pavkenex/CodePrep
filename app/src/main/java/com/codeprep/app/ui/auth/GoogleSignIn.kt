package com.codeprep.app.ui.auth

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.codeprep.app.BuildConfig
import com.codeprep.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException

@Composable
internal fun rememberGoogleSignInAction(
    onIdToken: (String) -> Unit,
    onFailure: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val googleSignInClient = remember(context) {
        resolveGoogleWebClientId(context)
            .takeIf { it.isNotBlank() }
            ?.let { webClientId ->
                val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
                GoogleSignIn.getClient(context, options)
            }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult

        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                onFailure()
            } else {
                onIdToken(idToken)
            }
        } catch (error: ApiException) {
            if (error.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                onFailure()
            }
        }
    }

    return {
        if (googleSignInClient == null) {
            onFailure()
        } else {
            launcher.launch(googleSignInClient.signInIntent)
        }
    }
}

internal fun signOutFromGoogle(
    context: Context,
    onComplete: () -> Unit
) {
    val webClientId = resolveGoogleWebClientId(context)
    if (webClientId.isBlank()) {
        onComplete()
        return
    }

    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()

    GoogleSignIn.getClient(context, options)
        .signOut()
        .addOnCompleteListener { onComplete() }
}

private fun resolveGoogleWebClientId(context: Context): String {
    val configuredClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
    if (configuredClientId.isNotBlank()) return configuredClientId

    val generatedResourceId = context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName
    )
    return generatedResourceId
        .takeIf { it != 0 }
        ?.let(context.resources::getString)
        .orEmpty()
}
