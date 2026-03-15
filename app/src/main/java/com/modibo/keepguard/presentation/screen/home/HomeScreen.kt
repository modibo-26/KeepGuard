package com.modibo.keepguard.presentation.screen.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.modibo.keepguard.domain.model.WarrantyStatus
import com.modibo.keepguard.domain.model.status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAssets: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    NotificationPermission()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("KeepGuard") })
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Stats cards
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Biens",
                        value = "${state.assetCount}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Garanties actives",
                        value = "${state.activeWarranties}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Entretiens à faire",
                        value = "${state.pendingMaintenances.size}",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Expiring warranties
                if (state.expiringWarranties.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Garanties qui expirent bientôt",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    state.expiringWarranties.forEach { warranty ->
                        val assetName = state.assets.find { it.id == warranty.assetId }?.name ?: ""
                        Card(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = "${warranty.type.label} — $assetName",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Expire le ${dateFormat.format(Date(warranty.endDate))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Expired warranties
                if (state.expiredWarranties > 0) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "${state.expiredWarranties} garantie(s) expirée(s)",
                            Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Pending maintenances
                if (state.pendingMaintenances.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Entretiens à faire",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    state.pendingMaintenances.forEach { maintenance ->
                        val assetName = state.assets.find { it.id == maintenance.assetId }?.name ?: ""
                        Card(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = maintenance.title,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (assetName.isNotEmpty()) {
                                    Text(
                                        text = assetName,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (maintenance.date > 0) {
                                    Text(
                                        text = dateFormat.format(Date(maintenance.date)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }

                // Empty state
                if (state.assetCount == 0) {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "Aucun bien enregistré",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Commencez par ajouter un bien dans l'onglet Mes biens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun NotificationPermission() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
