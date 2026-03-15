package com.modibo.keepguard.presentation.screen.assets.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AssetDetailScreen(
    viewModel: AssetDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEdit: (assetId: String) -> Unit,
    onDelete: () -> Unit,
    onWarranties: (assetId: String) -> Unit = {},
    onMaintenances: (assetId: String) -> Unit = {},
    onDocuments: (assetId: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAsset()
    }

    if (state.isDeleted) {
        LaunchedEffect(Unit) {
            onDelete()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail du bien") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    if (state.asset != null) {
                        IconButton(onClick = { onEdit(state.asset!!.id) }) {
                            Icon(Icons.Default.Edit, "Modifier")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.asset != null) {
            val asset = state.asset!!
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = asset.category.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (asset.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        asset.imageUrl,
                        asset.name,
                        Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(16.dp))

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        if (asset.brand.isNotEmpty()) {
                            DetailRow("Marque", asset.brand)
                        }
                        if (asset.model.isNotEmpty()) {
                            DetailRow("Modèle", asset.model)
                        }
                        if (asset.serialNumber.isNotEmpty()) {
                            DetailRow("N° de série", asset.serialNumber)
                        }
                        if (asset.purchasePlace.isNotEmpty()) {
                            DetailRow("Lieu d'achat", asset.purchasePlace)
                        }
                        if (asset.description.isNotEmpty()) {
                            DetailRow("Description", asset.description)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Associés", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onWarranties(asset.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Garanties")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onMaintenances(asset.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entretiens")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onDocuments(asset.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Documents")
                }
            }
            if (showDeleteDialog) {
                AlertDialog(
                    { showDeleteDialog = false },
                    {
                        TextButton(onClick = { viewModel.deleteAsset() }) {
                            Text("Supprimer")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Annuler")
                        }
                    },
                    title = { Text("Supprimer") },
                    text = { Text("Voulez-vous vraiment supprimer ce bien ?") },
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
