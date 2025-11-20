package com.github.fjbaldon.attendex.capture.core.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.fjbaldon.attendex.capture.feature.scanner.ScanMode
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    scanMode: ScanMode,
    onTextFound: (String) -> Unit,
    torchEnabled: Boolean,
    onTorchToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )

    // Hold reference to the provider so we can re-bind when mode changes
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    // Keep reference to the PreviewView to attach surface provider
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // 1. Permission Check
    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // 2. Torch Control
    LaunchedEffect(camera, torchEnabled) {
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    // 3. Bind Camera Logic (Runs when Provider, View, or Mode changes)
    LaunchedEffect(cameraProvider, previewView, scanMode) {
        val provider = cameraProvider
        val view = previewView

        if (provider != null && view != null) {
            val cameraExecutor = Executors.newSingleThreadExecutor()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = view.surfaceProvider
            }

            // DYNAMICALLY CHOOSE ANALYZER
            val analyzer = if (scanMode == ScanMode.QR) {
                BarcodeScanningAnalyzer(onTextFound)
            } else {
                TextRecognitionAnalyzer(onTextFound)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, analyzer)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
                onTorchToggle(camera?.cameraInfo?.hasFlashUnit() ?: false)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCamPermission) {
            // FIXED: Explicitly type AndroidView to <PreviewView>
            AndroidView<PreviewView>(
                factory = { ctx ->
                    val view = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    // Capture the view reference for the LaunchedEffect
                    previewView = view

                    // Initialize the Provider once
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            cameraProvider = cameraProviderFuture.get()
                        } catch (e: Exception) {
                            // Handle initialization error
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    view
                },
                modifier = Modifier.fillMaxSize()
            )

            // Visual Overlay sits on top
            CameraOverlay(scanMode = scanMode, modifier = Modifier.fillMaxSize())

        } else {
            PermissionRequiredScreen(onRequestPermission = {
                launcher.launch(Manifest.permission.CAMERA)
            })
        }
    }
}

@Composable
fun PermissionRequiredScreen(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Camera Permission Required")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}
