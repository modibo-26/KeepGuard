package com.modibo.keepguard.presentation.screen.assets.form

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.modibo.keepguard.domain.model.AssetCategory
import java.io.File

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
fun AssetFormScreen(
    viewModel: AssetFormViewModel = hiltViewModel(),
    onSaved: () -> Unit,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val imageToShow = state.imageUri ?: state.imageUrl.ifEmpty {null}
    var showSheet by remember { mutableStateOf(false) }
    val tempFile = File(context.cacheDir, "images/temp_photo.jpg").apply { parentFile?.mkdirs() }
    val tempUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)

    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {
            success -> if (success) viewModel.onImageUriChange(tempUri)
    }

    val galerie = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        uri -> uri?.let { viewModel.onImageUriChange(it) }
    }

    if (state.isSaved) {
        LaunchedEffect(Unit) {
            onSaved()
        }
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Modifier" else "Nouveau bien") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                state.name,
                { viewModel.onNameChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Nom du bien") }
            )
            Spacer(Modifier.height(8.dp))
            Text("Catégorie")
            FlowRow {
                AssetCategory.entries.forEach { category ->
                    FilterChip(
                        state.category == category,
                        { viewModel.onCategoryChange(category) },
                        { Text(category.label) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                state.description,
                { viewModel.onDescriptionChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Description") },
                minLines = 2
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                state.brand,
                { viewModel.onBrandChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Marque") }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                state.model,
                { viewModel.onModelChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Modèle") }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                state.serialNumber,
                { viewModel.onSerialNumberChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Numéro de série") }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                state.purchasePlace,
                { viewModel.onPurchasePlaceChange(it) },
                Modifier.fillMaxWidth(),
                label = { Text("Lieu d'achat") }
            )
            Box(
                modifier = Modifier.clickable { showSheet = true }
            ) {
                if (imageToShow != null) {
                    AsyncImage(
                        imageToShow,
                        "image",
                        Modifier.fillMaxWidth().height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, "Image")
                    Text("Ajouter une photo")
                }
            }
            if (showSheet) {
                ModalBottomSheet(onDismissRequest = { showSheet = false }) {
                    Row(Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            camera.launch(tempUri)
                            showSheet = false
                        }
                    ) {
                        Icon(Icons.Default.CameraAlt, "Caméra")
                        Text("Prendre une photo")
                    }
                    Row(Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            galerie.launch("image/*")
                            showSheet = false
                        }
                    ) {
                        Icon(Icons.Default.PhotoLibrary, "Galerie")
                        Text("Choisir dans la galerie")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.saveAsset() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.name.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Enregistrer")
                }
            }
        }
    }
}
