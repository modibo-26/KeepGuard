package com.modibo.keepguard.presentation.screen.warranty.detail

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
import com.modibo.keepguard.domain.model.WarrantyStatus
import com.modibo.keepguard.domain.model.status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WarrantyDetailScreen(
    viewModel: WarrantyDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEdit: (warrantyId: String, assetId: String) -> Unit,
    onDelete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    LaunchedEffect(Unit) {
        viewModel.loadWarranty()
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

    if (state.warranty != null) {
        val warranty = state.warranty!!
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Détail garantie") },
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
                    text = warranty.type.label,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = warranty.status.label,
                    color = when (warranty.status) {
                        WarrantyStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        WarrantyStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.error
                        WarrantyStatus.EXPIRED -> MaterialTheme.colorScheme.outline
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                if (warranty.provider.isNotEmpty()) {
                    Text(text = "Fournisseur : ${warranty.provider}")
                }
                Text(text = "Début : ${dateFormat.format(Date(warranty.startDate))}")
                Text(text = "Fin : ${dateFormat.format(Date(warranty.endDate))}")
                Text(text = "Durée : ${warranty.durationMonths} mois")
                if (warranty.conditions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Conditions : ${warranty.conditions}")
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onEdit(warranty.id, warranty.assetId) },
                    Modifier.fillMaxWidth()
                ) {
                    Text("Modifier")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.deleteWarranty() },
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
