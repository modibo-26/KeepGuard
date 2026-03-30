package com.modibo.keepguard.presentation.screen.document.form

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.modibo.keepguard.domain.model.DocumentType
import com.modibo.keepguard.presentation.component.ScannerButton

private val STEP_LABELS = listOf("Source", "Infos", "Lien", "Récap")

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun DocumentFormScreen(
    viewModel: DocumentFormViewModel = hiltViewModel(),
    onSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentStepIndex = DocumentFormStep.entries.indexOf(state.step)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    BackHandler(enabled = currentStepIndex > 0 && !(state.fromScanner && state.step == DocumentFormStep.INFO)) {
        viewModel.prevStep()
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDateChange(dateFormat.format(Date(it)))
                    }
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

    if (state.isSaved) {
        LaunchedEffect(Unit) {
            onSaved(state.assetId)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouveau document") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStepIndex > 0 && !(state.fromScanner && state.step == DocumentFormStep.INFO)) {
                            viewModel.prevStep()
                        } else {
                            onBack()
                        }
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
                        if (state.step == DocumentFormStep.RECAP) viewModel.saveDocument()
                        else viewModel.nextStep()
                    },
                    enabled = when (state.step) {
                        DocumentFormStep.SOURCE -> state.fileUri != null
                        DocumentFormStep.INFO -> state.name.isNotBlank()
                        DocumentFormStep.LINK -> true
                        DocumentFormStep.RECAP -> !state.isLoading
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (state.step == DocumentFormStep.RECAP) "Enregistrer" else "Suivant →",
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                // ─── STEP 1 : SOURCE ───
                DocumentFormStep.SOURCE -> {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Source du document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Scannez un document ou choisissez un fichier existant.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))

                        ScannerButton(
                            onScanResult = { uri -> viewModel.onFileSelected(uri) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { filePicker.launch("*/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (state.fileUri != null) "Fichier sélectionné" else "Choisir un fichier")
                        }
                    }
                }

                // ─── STEP 2 : INFO ───
                DocumentFormStep.INFO -> {
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Informations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.onNameChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nom du document") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        Text("Type de document")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DocumentType.entries.forEach { type ->
                                FilterChip(
                                    selected = state.type == type,
                                    onClick = { viewModel.onTypeChange(type) },
                                    label = { Text(type.label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.date,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Date du document") },
                            readOnly = true,
                            singleLine = true,
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
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.amount,
                            onValueChange = { viewModel.onAmountChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Montant") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.merchant,
                            onValueChange = { viewModel.onMerchantChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Commerce / Entreprise") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }

                // ─── STEP 3 : LIEN ───
                DocumentFormStep.LINK -> {
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Associer à...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Optionnel — liez ce document à un bien, une garantie ou un entretien.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        // Asset dropdown
                        if (state.assets.isNotEmpty()) {
                            var assetExpanded by remember { mutableStateOf(false) }
                            Text("Bien", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            ExposedDropdownMenuBox(assetExpanded, { assetExpanded = it }) {
                                TextField(
                                    value = state.assets.find { it.id == state.assetId }?.name ?: "Aucun",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(assetExpanded) }
                                )
                                ExposedDropdownMenu(assetExpanded, { assetExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Aucun") },
                                        onClick = {
                                            viewModel.onAssetSelected("")
                                            assetExpanded = false
                                        }
                                    )
                                    state.assets.forEach { asset ->
                                        DropdownMenuItem(
                                            text = { Text(asset.name) },
                                            onClick = {
                                                viewModel.onAssetSelected(asset.id)
                                                assetExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // Warranty dropdown
                        if (state.warranties.isNotEmpty()) {
                            var warrantyExpanded by remember { mutableStateOf(false) }
                            Text("Garantie", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            ExposedDropdownMenuBox(warrantyExpanded, { warrantyExpanded = it }) {
                                TextField(
                                    value = state.warranties.find { it.id == state.warrantyId }?.let {
                                        "${it.type.name} — ${it.provider.ifEmpty { "Sans fournisseur" }}"
                                    } ?: "Aucune",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(warrantyExpanded) }
                                )
                                ExposedDropdownMenu(warrantyExpanded, { warrantyExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Aucune") },
                                        onClick = {
                                            viewModel.onWarrantySelected("")
                                            warrantyExpanded = false
                                        }
                                    )
                                    state.warranties.forEach { warranty ->
                                        DropdownMenuItem(
                                            text = { Text("${warranty.type.name} — ${warranty.provider.ifEmpty { "Sans fournisseur" }}") },
                                            onClick = {
                                                viewModel.onWarrantySelected(warranty.id)
                                                warrantyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // Maintenance dropdown
                        if (state.maintenances.isNotEmpty()) {
                            var maintenanceExpanded by remember { mutableStateOf(false) }
                            Text("Entretien", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            ExposedDropdownMenuBox(maintenanceExpanded, { maintenanceExpanded = it }) {
                                TextField(
                                    value = state.maintenances.find { it.id == state.maintenanceId }?.title ?: "Aucun",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(maintenanceExpanded) }
                                )
                                ExposedDropdownMenu(maintenanceExpanded, { maintenanceExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Aucun") },
                                        onClick = {
                                            viewModel.onMaintenanceSelected("")
                                            maintenanceExpanded = false
                                        }
                                    )
                                    state.maintenances.forEach { maintenance ->
                                        DropdownMenuItem(
                                            text = { Text(maintenance.title) },
                                            onClick = {
                                                viewModel.onMaintenanceSelected(maintenance.id)
                                                maintenanceExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        if (state.assets.isEmpty() && state.warranties.isEmpty() && state.maintenances.isEmpty()) {
                            Text(
                                "Aucun bien, garantie ou entretien trouvé. Vous pourrez associer ce document plus tard.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }

                // ─── STEP 4 : RÉCAP ───
                DocumentFormStep.RECAP -> {
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Récapitulatif",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Vérifiez avant de valider.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        RecapCard("Document") {
                            if (state.name.isNotBlank()) RecapRow("Nom", state.name)
                            RecapRow("Type", state.type.label)
                            if (state.date.isNotBlank()) RecapRow("Date", state.date)
                            if (state.amount.isNotBlank()) RecapRow("Montant", state.amount)
                            if (state.merchant.isNotBlank()) RecapRow("Commerce", state.merchant)
                            RecapRow("Fichier", if (state.fileUri != null) "Sélectionné" else "Aucun")
                        }
                        Spacer(Modifier.height(10.dp))

                        RecapCard("Associations") {
                            val assetName = state.assets.find { it.id == state.assetId }?.name
                            RecapRow("Bien", assetName ?: "Aucun")

                            val warrantyLabel = state.warranties.find { it.id == state.warrantyId }?.let {
                                "${it.type.name} — ${it.provider.ifEmpty { "Sans fournisseur" }}"
                            }
                            RecapRow("Garantie", warrantyLabel ?: "Aucune")

                            val maintenanceTitle = state.maintenances.find { it.id == state.maintenanceId }?.title
                            RecapRow("Entretien", maintenanceTitle ?: "Aucun")
                        }

                        state.error?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error)
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
private fun RecapRow(label: String, value: String) {
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
