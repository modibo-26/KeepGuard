package com.modibo.keepguard.presentation.screen.scanner

import android.Manifest.permission
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel(),
    onCreateDocument: (Uri, String) -> Unit
) {

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val camera = ContextCompat.checkSelfPermission(context, permission.CAMERA)
    var hasPermission by remember { mutableStateOf(camera == PackageManager.PERMISSION_GRANTED) }


    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    val preview = remember { PreviewView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val capture = remember { ImageCapture.Builder().build() }
    if (hasPermission) {
        val ocrText = state.ocrText
        val uri = state.capturedImageUri
        Box(Modifier.fillMaxSize()) {
            if (ocrText != null && uri != null) {
                // Image capturée + bouton Enregistrer
                AsyncImage(
                    model = uri,
                    contentDescription = "Image scannée",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AndroidView(
                    factory = { preview },
                    modifier = Modifier.fillMaxSize()
                )
                LaunchedEffect(Unit) {
                    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                    val previewUseCase = Preview.Builder().build().also {
                        it.surfaceProvider = preview.surfaceProvider
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        previewUseCase,
                        capture
                    )
                }
            }
            if (state.isProcessing) {
                CircularProgressIndicator()
            }
            if(ocrText != null && uri != null) {
                Column(Modifier.align(Alignment.BottomCenter)) {
                    Button({
                        onCreateDocument(uri, ocrText)
                    }) {
                        Text("Enregistrer le document")
                    }
                    Button({ viewModel.resetScan() }) {
                        Text("Reprendre la photo")
                    }
                }
            } else {
                Button(
                    {
                        val file = File.createTempFile("scan", ".jpg", context.cacheDir)
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                                    viewModel.onImageCaptured(Uri.fromFile(file))
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    // handle error
                                }
                            },
                        )
                    },
                    Modifier.align(Alignment.BottomCenter)
                ) { Text("Scanner") }
            }
        }
    } else {
        LaunchedEffect(Unit) {
            permissionsLauncher.launch(permission.CAMERA)
        }
    }
}