package com.modibo.keepguard.presentation.screen.assets.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AssetDetailScreen(
    viewModel: AssetDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEdit: (assetId: String) -> Unit,
    onDelete: () -> Unit,
    onWarranties: (assetId: String) -> Unit = {},
    onMaintenances: (assetId: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAsset()
    }

    if (state.isDeleted) {
        LaunchedEffect(Unit) {
            onDelete()
        }
        return
    }

    if (state.isLoading) {
        Box() {
            CircularProgressIndicator()
        }
    }

    if (state.asset != null) {
        val asset = state.asset!!
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Détail du bien") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding)) {
                Text(text = asset.name)
                Text(text = asset.category.label)
                Text(text = asset.brand)
                Text(text = asset.model)
                Text(text = asset.serialNumber)
                Text(text = asset.purchasePlace)
                Button({ onWarranties(asset.id) }) {
                    Text("Garanties")
                }
                Button({ onMaintenances(asset.id) }) {
                    Text("Entretiens")
                }
                Button({ onEdit(asset.id) }) {
                    Text("Modifier")
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    }
                }
                Button({ viewModel.deleteAsset() }) {
                    Text("Supprimer")
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

}