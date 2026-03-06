package com.modibo.keepguard.presentation.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
//    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.authSuccess) {
        if (state.authSuccess) {
            snackBarHostState.showSnackbar("Compte lié avec succès")
            viewModel.clearAuthSuccess()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Paramètres") }) },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profil
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profil", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (state.user?.isAnonymous == true) {
                        Text("Compte anonyme")
                        Text(
                            "Liez votre compte pour ne pas perdre vos données",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(state.user?.email ?: "")
                        if (!state.user?.displayName.isNullOrEmpty()) {
                            Text(state.user?.displayName ?: "")
                        }
                    }
                }
            }

            if (state.user?.isAnonymous == true) {
                // Lier compte (seulement si anonyme)
                Spacer(Modifier.height(24.dp))
                Text("Se connecter ou lier un compte", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                // Email + Password
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Mot de passe") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.linkWithEmail(state.email, state.password) },
                    enabled = state.email.isNotBlank() && state.password.isNotBlank() && !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Lier avec Email")
                }

                Spacer(Modifier.height(16.dp))

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.signInWithEmail(state.email, state.password) },
                    enabled = state.email.isNotBlank() && state.password.isNotBlank() && !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Se connecter avec Email")
                }

                Spacer(Modifier.height(16.dp))

                // Google
                OutlinedButton(
                    onClick = { viewModel.logWithGoogle() },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("S'authentifier avec Google")
                }
            }

            if (state.isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(Modifier.height(32.dp))

            // Déconnexion
//            Button(
//                onClick = {
//                    viewModel.signOut()
//                    onSignOut()
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.error
//                ),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Icon(Icons.Default.Logout, contentDescription = null)
//                Spacer(Modifier.padding(4.dp))
//                Text("Se déconnecter")
//            }
        }
    }
}
