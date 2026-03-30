package com.modibo.keepguard.presentation.screen.maintenance.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.modibo.keepguard.domain.model.MaintenanceType
import com.modibo.keepguard.presentation.component.ScannerButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val STEP_LABELS = listOf("Source", "Infos", "Récap")

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun MaintenanceFormScreen(
    viewModel: MaintenanceFormViewModel = hiltViewModel(),
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.date
    )
    val currentStepIndex = MaintenanceFormStep.entries.indexOf(state.step)

    BackHandler(enabled = currentStepIndex > 0 && !(state.isEditing && state.step == MaintenanceFormStep.INFO)) {
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
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDateChange(it)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Modifier l'entretien" else "Nouvel entretien") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStepIndex > 0 && !(state.isEditing && state.step == MaintenanceFormStep.INFO)) {
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
                        if (state.step == MaintenanceFormStep.RECAP) viewModel.saveMaintenance()
                        else viewModel.nextStep()
                    },
                    enabled = when (state.step) {
                        MaintenanceFormStep.SOURCE -> true
                        MaintenanceFormStep.INFO -> state.title.isNotEmpty() && state.date != null
                        MaintenanceFormStep.RECAP -> !state.isLoading
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
                            if (state.step == MaintenanceFormStep.RECAP) "Enregistrer" else "Suivant →",
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

                when (state.step) {
                    // ─── STEP 1 : SOURCE ───
                    MaintenanceFormStep.SOURCE -> {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Source",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Scannez un document d'entretien ou saisissez manuellement.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))

                            ScannerButton(
                                onScanResult = { uri -> viewModel.onScanResult(uri) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ─── STEP 2 : INFO ───
                    MaintenanceFormStep.INFO -> {
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
                                value = state.title,
                                onValueChange = { viewModel.onTitleChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Titre") },
                                singleLine = true
                            )
                            Spacer(Modifier.height(12.dp))

                            Text("Type d'entretien")
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MaintenanceType.entries.forEach { type ->
                                    FilterChip(
                                        selected = state.type == type,
                                        onClick = { viewModel.onTypeChange(type) },
                                        label = { Text(type.label) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = if (state.date != null) dateFormat.format(Date(state.date!!)) else "",
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Date") },
                                readOnly = true,
                                singleLine = true,
                                interactionSource = remember { MutableInteractionSource() }
                                    .also { interactionSource ->
                                        LaunchedEffect(interactionSource) {
                                            interactionSource.interactions.collect {
                                                if (it is PressInteraction.Release) {
                                                    showDatePicker = true
                                                }
                                            }
                                        }
                                    }
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { viewModel.onDescriptionChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Description") },
                                minLines = 2
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = state.provider,
                                onValueChange = { viewModel.onProviderChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Prestataire") },
                                singleLine = true
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = state.cost,
                                onValueChange = { viewModel.onCostChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Coût (€)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = state.mileage,
                                onValueChange = { viewModel.onMileageChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Kilométrage") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            Spacer(Modifier.height(12.dp))

                            if (state.type == MaintenanceType.RECURRING) {
                                OutlinedTextField(
                                    value = state.recurrenceMonths,
                                    onValueChange = { viewModel.onRecurrenceChange(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Récurrence (mois)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = state.isCompleted,
                                    onCheckedChange = { viewModel.onCompletedChange(it) }
                                )
                                Text("Entretien effectué")
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    // ─── STEP 3 : RÉCAP ───
                    MaintenanceFormStep.RECAP -> {
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

                            RecapCard("Entretien") {
                                if (state.title.isNotBlank()) RecapRow("Titre", state.title)
                                RecapRow("Type", state.type.label)
                                state.date?.let { RecapRow("Date", dateFormat.format(Date(it))) }
                                if (state.description.isNotBlank()) RecapRow("Description", state.description)
                                if (state.provider.isNotBlank()) RecapRow("Prestataire", state.provider)
                                if (state.cost.isNotBlank()) RecapRow("Coût", "${state.cost} €")
                                if (state.mileage.isNotBlank()) RecapRow("Kilométrage", "${state.mileage} km")
                                if (state.type == MaintenanceType.RECURRING && state.recurrenceMonths.isNotBlank()) {
                                    RecapRow("Récurrence", "${state.recurrenceMonths} mois")
                                }
                                RecapRow("Effectué", if (state.isCompleted) "Oui" else "Non")
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
