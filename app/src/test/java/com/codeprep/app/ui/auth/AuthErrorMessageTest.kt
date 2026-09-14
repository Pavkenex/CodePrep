package com.codeprep.app.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorMessageTest {

    @Test
    fun googleAccountConflict_usesActionableMessage() {
        assertEquals(
            "Use email and password",
            resolveGoogleAuthErrorMessage(
                errorCode = "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
                errorMessage = "Firebase error",
                genericMessage = "Google sign-in failed",
                accountExistsMessage = "Use email and password"
            )
        )
    }

    @Test
    fun otherGoogleError_preservesProviderMessage() {
        assertEquals(
            "Network error",
            resolveGoogleAuthErrorMessage(
                errorCode = "ERROR_NETWORK_REQUEST_FAILED",
                errorMessage = "Network error",
                genericMessage = "Google sign-in failed",
                accountExistsMessage = "Use email and password"
            )
        )
    }

    @Test
    fun missingGoogleErrorMessage_usesGenericMessage() {
        assertEquals(
            "Google sign-in failed",
            resolveGoogleAuthErrorMessage(
                errorCode = null,
                errorMessage = null,
                genericMessage = "Google sign-in failed",
                accountExistsMessage = "Use email and password"
            )
        )
    }
}
