package com.modibo.keepguard.presentation.screen.maintenance.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MaintenanceDetailScreen(
    viewModel: MaintenanceDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEdit: (assetId: String, maintenanceId: String) -> Unit,
    onDelete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    if (state.isDeleted) {
        LaunchedEffect(Unit) {
            onDelete()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail entretien") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    if (state.maintenance != null) {
                        IconButton(onClick = { onEdit(state.maintenance!!.assetId, state.maintenance!!.id) }) {
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
        } else if (state.maintenance != null) {
            val maintenance = state.maintenance!!
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = maintenance.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = if (maintenance.isCompleted) "Fait" else "A faire",
                    color = if (maintenance.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        DetailRow("Type", maintenance.type.label)
                        DetailRow("Date", dateFormat.format(Date(maintenance.date)))
                        if (maintenance.description.isNotEmpty()) {
                            DetailRow("Description", maintenance.description)
                        }
                        if (maintenance.provider.isNotEmpty()) {
                            DetailRow("Prestataire", maintenance.provider)
                        }
                        if (maintenance.cost != null) {
                            DetailRow("Coût", "${maintenance.cost} €")
                        }
                        if (maintenance.mileage != null) {
                            DetailRow("Kilométrage", "${maintenance.mileage} km")
                        }
                        if (maintenance.recurrenceMonths != null) {
                            DetailRow("Récurrence", "Tous les ${maintenance.recurrenceMonths} mois")
                        }
                        if (maintenance.nextDueDate != null) {
                            DetailRow("Prochain", dateFormat.format(Date(maintenance.nextDueDate)))
                        }
                        if (maintenance.nextDueMileage != null) {
                            DetailRow("Prochain km", "${maintenance.nextDueMileage} km")
                        }
                    }
                }
            }
        }
        if (showDeleteDialog) {
            AlertDialog(
                { showDeleteDialog = false },
                {
                    TextButton(onClick = { viewModel.deleteMaintenance() }) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Annuler")
                    }
                },
                title = { Text("Supprimer") },
                text = { Text("Voulez-vous vraiment supprimer cet entretien ?") },
            )
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
