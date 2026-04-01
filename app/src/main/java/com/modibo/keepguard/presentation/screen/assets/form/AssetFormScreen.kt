package com.modibo.keepguard.presentation.screen.assets.form

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.modibo.keepguard.domain.model.AssetCategory
import com.modibo.keepguard.domain.model.AssetCondition
import com.modibo.keepguard.domain.model.AssetSubCategory
import com.modibo.keepguard.domain.model.ConsumerRights
import com.modibo.keepguard.domain.model.PurchaseMode
import com.modibo.keepguard.domain.model.RightType
import com.modibo.keepguard.presentation.component.ScannerButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val STEP_LABELS = listOf("Photo", "Catégorie", "Infos", "Récap")

private val CATEGORY_ICONS = mapOf(
    AssetCategory.APPLIANCE to "🏠",
    AssetCategory.VEHICLE to "🚗",
    AssetCategory.TECH to "💻",
    AssetCategory.FURNITURE to "🛋️",
    AssetCategory.OTHER to "📦"
)

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
fun AssetFormScreen(
    viewModel: AssetFormViewModel = hiltViewModel(),
    onSaved: () -> Unit,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val context = LocalContext.current
    val imageToShow = state.imageUri ?: state.imageUrl.ifEmpty { null }
    val tempFile = remember { File(context.cacheDir, "images/temp_photo_${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() } }
    val tempUri = remember { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile) }
    val currentStepIndex = AssetFormStep.entries.indexOf(state.step)

    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.onImageUriChange(tempUri)
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) camera.launch(tempUri)
    }

    val galerie = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageUriChange(it) }
    }

    BackHandler(enabled = currentStepIndex > 0) {
        viewModel.prevStep()
    }

    if (state.isSaved) {
        LaunchedEffect(Unit) { onSaved() }
        return
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onPurchaseDateChange(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Modifier" else "Nouveau bien") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStepIndex > 0) viewModel.prevStep() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    Text(
                        "${currentStepIndex + 1} / ${STEP_LABELS.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }
            )
        },
        bottomBar = {
            Column(Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (state.step == AssetFormStep.RECAP) viewModel.saveAsset()
                        else viewModel.nextStep()
                    },
                    enabled = when (state.step) {
                        AssetFormStep.PHOTO -> true
                        AssetFormStep.CATEGORY -> state.subCategory != null
                        AssetFormStep.INFO_PURCHASE -> state.name.isNotBlank()
                        AssetFormStep.RECAP -> !state.isLoading
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (state.step == AssetFormStep.RECAP) "Enregistrer" else "Suivant →",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            Column {
                // Stepper
                StepperRow(currentStepIndex)

                // Content
                when (state.step) {
                // ─── STEP 1 : PHOTO ───
                AssetFormStep.PHOTO -> {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SectionTitle("Photo du bien")
                        Text(
                            "Prenez une photo ou choisissez depuis la galerie. Optionnel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                2.dp,
                                if (imageToShow != null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(
                                Modifier.fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageToShow != null) {
                                    AsyncImage(
                                        imageToShow, "image",
                                        Modifier.fillMaxWidth().height(220.dp)
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Image, "Image",
                                            Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedCard(
                                modifier = Modifier.weight(1f).clickable {
                                    cameraPermission.launch(android.Manifest.permission.CAMERA)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CameraAlt, "Caméra", Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Caméra", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                            OutlinedCard(
                                modifier = Modifier.weight(1f).clickable {
                                    galerie.launch("image/*")
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, "Galerie", Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Galerie", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // ─── STEP 2 : CATÉGORIE ───
                AssetFormStep.CATEGORY -> {
                    Column(
                        Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
                    ) {
                        SectionTitle("Catégorie")
                        Spacer(Modifier.height(8.dp))

                        // Grille 2 colonnes
                        val categories = AssetCategory.entries
                        for (i in categories.indices step 2) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CategoryCard(
                                    category = categories[i],
                                    icon = CATEGORY_ICONS[categories[i]] ?: "📦",
                                    isSelected = state.category == categories[i],
                                    onClick = { viewModel.onCategoryChange(categories[i]) },
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < categories.size) {
                                    CategoryCard(
                                        category = categories[i + 1],
                                        icon = CATEGORY_ICONS[categories[i + 1]] ?: "📦",
                                        isSelected = state.category == categories[i + 1],
                                        onClick = { viewModel.onCategoryChange(categories[i + 1]) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        Spacer(Modifier.height(16.dp))
                        SectionTitle("Sous-catégorie")
                        Spacer(Modifier.height(8.dp))

                        AssetSubCategory.forCategory(state.category).forEach { sub ->
                            SubCategoryItem(
                                label = sub.label,
                                isSelected = state.subCategory == sub,
                                onClick = { viewModel.onSubCategoryChange(sub) }
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ─── STEP 3 : INFOS + ACHAT ───
                AssetFormStep.INFO_PURCHASE -> {
                    Column(
                        Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
                    ) {
                        SectionTitle("Informations")
                        Spacer(Modifier.height(8.dp))

                        ScannerButton(
                            onScanResult = { uri -> viewModel.onImageCaptured(uri) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (state.scannedDocumentUri != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = state.scannedDocumentUri,
                                    contentDescription = "Document scanné",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    "Document scanné ✓",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            state.name, { viewModel.onNameChange(it) },
                            Modifier.fillMaxWidth(),
                            label = { Text("Nom du bien") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            state.brand, { viewModel.onBrandChange(it) },
                            Modifier.fillMaxWidth(),
                            label = { Text("Marque") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            state.model, { viewModel.onModelChange(it) },
                            Modifier.fillMaxWidth(),
                            label = { Text("Modèle") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            state.serialNumber, { viewModel.onSerialNumberChange(it) },
                            Modifier.fillMaxWidth(),
                            label = { Text("Numéro de série") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            state.description, { viewModel.onDescriptionChange(it) },
                            Modifier.fillMaxWidth(),
                            label = { Text("Remarques") },
                            minLines = 2
                        )

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                        SectionTitle("Achat")
                        Spacer(Modifier.height(8.dp))

                        SectionLabel("Mode d'achat")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PurchaseMode.entries.forEach { mode ->
                                FilterChip(
                                    state.purchaseMode == mode,
                                    { viewModel.onPurchaseModeChange(mode) },
                                    { Text(mode.label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        SectionLabel("État")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssetCondition.entries.forEach { cond ->
                                FilterChip(
                                    state.condition == cond,
                                    { viewModel.onConditionChange(cond) },
                                    { Text(cond.label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = if (state.purchaseDate != null) dateFormat.format(Date(state.purchaseDate!!)) else "",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Date d'achat") },
                            readOnly = true,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect {
                                            if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                                showDatePicker = true
                                            }
                                        }
                                    }
                                }
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            state.purchasePlace, { viewModel.onPurchasePlaceChange(it) },
                            Modifier.fillMaxWidth(),
                            label = { Text("Vendeur / Lieu d'achat") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            state.purchasePrice?.toString() ?: "",
                            onValueChange = { viewModel.onPurchasePriceChange(it.toDoubleOrNull()) },
                            Modifier.fillMaxWidth(),
                            label = { Text("Prix (€)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                        SectionTitle("Garantie constructeur")
                        Text(
                            "Optionnelle — proposée en plus par le vendeur ou fabricant.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        val durees = listOf(
                            null to "Non renseignée", 12 to "1 an", 24 to "2 ans",
                            36 to "3 ans", 60 to "5 ans"
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            durees.forEach { duree ->
                                FilterChip(
                                    state.warrantyMonths == duree.first,
                                    { viewModel.onWarrantyMonthsChange(duree.first) },
                                    { Text(duree.second) }
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }

                // ─── STEP 4 : RÉCAP ───
                AssetFormStep.RECAP -> {
                    Column(
                        Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
                    ) {
                        SectionTitle("Récapitulatif")
                        Text(
                            "Vérifiez avant de valider.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        // Catégorie
                        RecapCard("Catégorie") {
                            DetailRow("Type", state.category.label)
                            state.subCategory?.let { DetailRow("Sous-catégorie", it.label) }
                        }
                        Spacer(Modifier.height(10.dp))

                        // Infos
                        RecapCard("Informations") {
                            if (state.name.isNotBlank()) DetailRow("Nom", state.name)
                            if (state.brand.isNotBlank()) DetailRow("Marque", state.brand)
                            if (state.model.isNotBlank()) DetailRow("Modèle", state.model)
                            if (state.serialNumber.isNotBlank()) DetailRow("N° de série", state.serialNumber)
                            if (state.description.isNotBlank()) DetailRow("Description", state.description)
                        }
                        Spacer(Modifier.height(10.dp))

                        // Achat
                        RecapCard("Achat") {
                            DetailRow("Mode d'achat", state.purchaseMode.label)
                            DetailRow("État", state.condition.label)
                            state.purchaseDate?.let { DetailRow("Date d'achat", dateFormat.format(Date(it))) }
                            state.purchasePrice?.let { DetailRow("Prix", "$it €") }
                            if (state.purchasePlace.isNotBlank()) DetailRow("Lieu d'achat", state.purchasePlace)
                            state.warrantyMonths?.let {
                                val label = if (it >= 12 && it % 12 == 0) "${it / 12} an${if (it > 12) "s" else ""}" else "$it mois"
                                DetailRow("Garantie constructeur", label)
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Droits consommateur
                        SectionTitle("Vos droits")
                        Text(
                            "Informations indicatives basées sur la législation française.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))

                        val purchaseDateStr = state.purchaseDate?.let {
                            SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(Date(it))
                        }
                        val rights = ConsumerRights.forCategory(
                            category = state.category,
                            condition = state.condition,
                            purchaseMode = state.purchaseMode,
                            purchaseDate = purchaseDateStr
                        )
                        rights.forEach { right ->
                            RightCard(right, state.purchaseDate, dateFormat)
                            Spacer(Modifier.height(6.dp))
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
            }
            if (state.isLoading) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// ─── COMPOSABLES PRIVÉS ──────────────────────────────────────────

@Composable
private fun StepperRow(currentStep: Int) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        STEP_LABELS.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(28.dp)
                        .background(
                            when {
                                index < currentStep -> MaterialTheme.colorScheme.primary
                                index == currentStep -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentStep) {
                        Icon(
                            Icons.Default.Check, "Done",
                            Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (index == currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    label,
                    fontSize = 10.sp,
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                    color = if (index <= currentStep) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (index < STEP_LABELS.size - 1) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(bottom = 12.dp)
                        .background(
                            if (index < currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun CategoryCard(
    category: AssetCategory,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            2.dp,
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                category.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SubCategoryItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            2.dp,
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check, "Sélectionné",
                    Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RecapCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun RightCard(
    right: com.modibo.keepguard.domain.model.ConsumerRight,
    purchaseDate: Long?,
    dateFormat: SimpleDateFormat
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (right.type) {
                RightType.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                RightType.WARNING -> MaterialTheme.colorScheme.errorContainer
                RightType.INFORMATIVE -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(right.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(right.description, style = MaterialTheme.typography.bodySmall)
            Text(
                right.source,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (right.durationMonths != null && purchaseDate != null) {
                val expiryMillis = purchaseDate + (right.durationMonths * 30.44 * 24 * 60 * 60 * 1000).toLong()
                val expiryStr = dateFormat.format(Date(expiryMillis))
                val daysLeft = ((expiryMillis - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
                val color = when {
                    daysLeft < 0 -> MaterialTheme.colorScheme.error
                    daysLeft < 60 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (daysLeft < 0) "Expirée le $expiryStr" else "Expire le $expiryStr",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
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
