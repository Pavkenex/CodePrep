package com.codeprep.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.components.GamifiedTextField
import com.codeprep.app.ui.theme.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.codeprep.app.ui.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("main") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo / Title
        Text(
            "codePrep",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = ElectricCyan
            ),
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = IceWhite
            ),
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.Start)
        )

        GamifiedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Email or username",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.padding(bottom = 16.dp),
            backgroundColor = Charcoal,
            textColor = IceWhite,
            cursorColor = ElectricCyan,
            placeholderColor = TextLight,
            borderColor = ElectricCyan
        )

        GamifiedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.padding(bottom = 24.dp),
            backgroundColor = Charcoal,
            textColor = IceWhite,
            cursorColor = ElectricCyan,
            placeholderColor = TextLight,
            borderColor = ElectricCyan
        )

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = CardinalRed,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        GamifiedButton(
            text = "LOGIN",
            onClick = { viewModel.login(email, password) },
            backgroundColor = ElectricCyan,
            shadowColor = SkyBlueDark,
            textColor = Charcoal,
            enabled = authState !is AuthState.Loading,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GamifiedButton(
            text = "LOGIN WITH GOOGLE",
            onClick = { /* TODO: Google Auth */ },
            backgroundColor = Charcoal,
            shadowColor = DeepCharcoal,
            textColor = IceWhite,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "DON'T HAVE AN ACCOUNT?",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "SIGN UP",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                ),
                modifier = Modifier.clickable { navController.navigate(Screen.Register.route) }
            )
        }
    }
}
