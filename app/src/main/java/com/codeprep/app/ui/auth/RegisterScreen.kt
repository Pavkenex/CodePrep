package com.codeprep.app.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.components.GamifiedTextField
import com.codeprep.app.ui.theme.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.codeprep.app.R
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.navigation.Screen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("main") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    AuthScreenFrame(showBrand = true) {
        Text(
            text = localizedStringResource(R.string.auth_register_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = ElectricCyan
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        GamifiedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = localizedStringResource(R.string.auth_register_placeholder_full_name),
            modifier = Modifier.padding(bottom = 16.dp),
            backgroundColor = Charcoal,
            textColor = IceWhite,
            cursorColor = ElectricCyan,
            placeholderColor = TextLight,
            borderColor = ElectricCyan
        )

        GamifiedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = localizedStringResource(R.string.auth_register_placeholder_email),
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
            placeholder = localizedStringResource(R.string.auth_register_placeholder_password),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.padding(bottom = 16.dp),
            backgroundColor = Charcoal,
            textColor = IceWhite,
            cursorColor = ElectricCyan,
            placeholderColor = TextLight,
            borderColor = ElectricCyan
        )

        GamifiedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = localizedStringResource(R.string.auth_register_placeholder_confirm_password),
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
            text = localizedStringResource(R.string.auth_register_create_account),
            onClick = {
                if (password == confirmPassword) {
                    viewModel.register(fullName, email, password)
                }
            },
            backgroundColor = ElectricCyan,
            shadowColor = SkyBlueDark,
            textColor = Charcoal,
            enabled = authState !is AuthState.Loading,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GamifiedButton(
            text = localizedStringResource(R.string.auth_register_google),
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
                text = localizedStringResource(R.string.auth_register_have_account),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextLight
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = localizedStringResource(R.string.auth_login_title),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = ElectricCyan
                ),
                modifier = Modifier.clickable { navController.navigate(Screen.Login.route) }
            )
        }
    }
}
