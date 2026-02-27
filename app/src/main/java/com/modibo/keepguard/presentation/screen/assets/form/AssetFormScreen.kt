package com.modibo.keepguard.presentation.screen.assets.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.modibo.keepguard.domain.model.AssetCategory

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun AssetFormScreen(
    viewModel: AssetFormViewModel = hiltViewModel(),
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    if (state.isSaved) {
        LaunchedEffect(Unit) {
            onSaved()
        }
        return
    }
    Scaffold() { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                state.name,
                { viewModel.onNameChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Nom du bien") }
            )
            FlowRow {
                AssetCategory.entries.forEach { category ->
                    FilterChip(
                        state.category == category,
                        { viewModel.onCategoryChange(category) },
                        { Text(category.label) }
                    )
                }
            }
            OutlinedTextField(
                state.description,
                { viewModel.onDescriptionChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Description") }
            )
            OutlinedTextField(
                state.brand,
                { viewModel.onBrandChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Marque") }
            )
            OutlinedTextField(
                state.model,
                { viewModel.onModelChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Modèle") }
            )
            OutlinedTextField(
                state.serialNumber,
                { viewModel.onSerialNumberChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Numéro de série") }
            )
            OutlinedTextField(
                state.purchasePlace,
                { viewModel.onPurchasePlaceChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Lieu d'achat") }
            )
            Button({ viewModel.saveAsset() }) {
                Text("Enregistrer")
                if (state.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}