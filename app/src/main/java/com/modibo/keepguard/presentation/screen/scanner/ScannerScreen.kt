package com.modibo.keepguard.presentation.screen.scanner

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.presentation.component.ScannerButton

@Composable
fun ScannerScreen(
    toAsset: (Uri, ScannedData) -> Unit,
    toDocument: (Uri, ScannedData) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
) {

    val state by viewModel.state.collectAsState()

    val scanned = state.scanned
    val uri = state.capturedImageUri
    Box(Modifier.fillMaxSize()) {
        if (scanned != null && uri != null) {
            // Image capturée + bouton Enregistrer
            AsyncImage(
                model = uri,
                contentDescription = "Image scannée",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Column(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    { toAsset(uri, scanned) }
                ) {
                    Text("Créer un bien")
                }
                Button(
                    { toDocument(uri, scanned) }
                ) {
                    Text("Enregistrer comme document")
                }
                OutlinedButton({ viewModel.resetScan() }) {
                    Text("Reprendre la photo")
                }
            }
        } else {
            ScannerButton({
                uri -> viewModel.onImageCaptured(uri)
            })
        }
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}