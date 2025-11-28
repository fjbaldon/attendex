package com.github.fjbaldon.attendex.capture.feature.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.fjbaldon.attendex.capture.ui.common.BrandingHeader
import com.github.fjbaldon.attendex.capture.ui.common.FormErrorMessage

@Composable
fun LoginScreen(
    onLoginSuccess: (Boolean) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Animation state
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Watch for state changes to trigger navigation
    LaunchedEffect(uiState.isLoggedIn, uiState.requirePasswordChange) {
        if (uiState.requirePasswordChange) {
            onLoginSuccess(true) // Navigate to Change Password
        } else if (uiState.isLoggedIn) {
            onLoginSuccess(false) // Navigate to Event List
        }
    }

    if (uiState.showUserMismatchDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelWipe() },
            title = { Text("Warning: Data Loss") },
            text = {
                Text("You are logging in as a different user. There are unsynced scans on this device from the previous user.\n\nLogging in will PERMANENTLY DELETE those scans.\n\nAre you sure?")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmWipeData() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Data & Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelWipe() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold { padding ->
        // Gradient Background Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.Center), // Center vertically
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Header Entry
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BrandingHeader(title = "AttendEx")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sign in to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Card-like surface for inputs to pop against gradient
                Surface(
                    color = Color.Transparent, // Keep transparent or add background if gradient is too strong
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            isError = uiState.error != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.error != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                                val description = if (passwordVisible) "Hide password" else "Show password"

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                FormErrorMessage(error = uiState.error)

                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Sign In")
                    }
                }
            }
        }
    }
}
