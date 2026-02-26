package com.modibo.keepguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.modibo.keepguard.presentation.screen.splash.SplashViewModel
import com.modibo.keepguard.ui.theme.KeepGuardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeepGuardTheme {
                val viewModel: SplashViewModel = hiltViewModel()
                val isReady by viewModel.isReady.collectAsState()
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isReady) {
                        Text("Connecté ! Auth Anonyme OK")
                    } else {
                        Text("Connexion en cours...")
                    }
                }
            }
        }
    }
}