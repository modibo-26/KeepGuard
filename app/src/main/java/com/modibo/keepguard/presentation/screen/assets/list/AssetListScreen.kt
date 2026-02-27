package com.modibo.keepguard.presentation.screen.assets.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AssetListScreen(
    viewModel: AssetListViewModel = hiltViewModel(),
    onAssetClick: (String) -> Unit,
    onAddClick: () -> Unit
){
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAssets()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Ajouter")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.padding(padding)) {
                CircularProgressIndicator()
            }
        } else if (state.assets.isEmpty()) {
            Box(Modifier.padding(padding)) {
                Text("Aucun bien")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(state.assets) { asset ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onAssetClick(asset.id) }
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text(text = asset.name)
                            Text(text = asset.category.label)
                            Text(text = asset.model)
                        }
                    }
                }
            }
        }
    }
}
