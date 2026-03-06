package com.modibo.keepguard.presentation.screen.maintenance.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    onEdit: (maintenanceId: String, assetId: String) -> Unit,
    onDelete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    LaunchedEffect(Unit) {
        viewModel.loadMaintenance()
    }

    if (state.isDeleted) {
        LaunchedEffect(Unit) {
            onDelete()
        }
        return
    }

    if (state.isLoading) {
        Box {
            CircularProgressIndicator()
        }
    }

    if (state.maintenance != null) {
        val maintenance = state.maintenance!!
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Détail entretien") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text(
                    text = maintenance.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (maintenance.isCompleted) "Fait" else "A faire",
                    color = if (maintenance.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                Text(text = "Type : ${maintenance.type.label}")
                Text(text = "Date : ${dateFormat.format(Date(maintenance.date))}")
                if (maintenance.description.isNotEmpty()) {
                    Text(text = "Description : ${maintenance.description}")
                }
                if (maintenance.provider.isNotEmpty()) {
                    Text(text = "Prestataire : ${maintenance.provider}")
                }
                if (maintenance.cost != null) {
                    Text(text = "Coût : ${maintenance.cost} €")
                }
                if (maintenance.mileage != null) {
                    Text(text = "Kilométrage : ${maintenance.mileage} km")
                }
                if (maintenance.recurrenceMonths != null) {
                    Text(text = "Récurrence : tous les ${maintenance.recurrenceMonths} mois")
                }
                if (maintenance.nextDueDate != null) {
                    Text(text = "Prochain : ${dateFormat.format(Date(maintenance.nextDueDate))}")
                }
                if (maintenance.nextDueMileage != null) {
                    Text(text = "Prochain km : ${maintenance.nextDueMileage} km")
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onEdit(maintenance.id, maintenance.assetId) },
                    Modifier.fillMaxWidth()
                ) {
                    Text("Modifier")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.deleteMaintenance() },
                    Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            }
        }
    }
}
