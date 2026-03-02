package com.modibo.keepguard.presentation.screen.maintenance.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
fun MaintenanceListScreen(
    viewModel: MaintenanceListViewModel = hiltViewModel(),
    onMaintenanceClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    LaunchedEffect(Unit) {
        viewModel.loadMaintenances()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entretiens") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Ajouter un entretien")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.padding(padding)) {
                CircularProgressIndicator()
            }
        } else if (state.maintenances.isEmpty()) {
            Box(Modifier.padding(padding).padding(16.dp)) {
                Text("Aucun entretien pour ce bien")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(state.maintenances) { maintenance ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onMaintenanceClick(maintenance.id) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = maintenance.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (maintenance.isCompleted) "Fait" else "A faire",
                                    color = if (maintenance.isCompleted)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                            Text(text = maintenance.type.label)
                            if (maintenance.provider.isNotEmpty()) {
                                Text(text = maintenance.provider)
                            }
                            Text(
                                text = dateFormat.format(Date(maintenance.date)),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (maintenance.cost != null) {
                                Text(
                                    text = "${maintenance.cost} €",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
