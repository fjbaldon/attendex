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
            ContextCompat.checkSelfPermission(
                context, // This usage makes the initializer NOT redundant
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )

    // Hold references to re-bind camera when ScanMode changes
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // 1. Check Permissions
    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // 2. Handle Torch
    LaunchedEffect(camera, torchEnabled) {
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    // 3. Bind Camera (Runs when Provider, View, or ScanMode changes)
    // This replaces the 'key(scanMode)' wrapper
    LaunchedEffect(cameraProvider, previewView, scanMode) {
        val provider = cameraProvider
        val view = previewView

        if (provider != null && view != null) {
            val cameraExecutor = Executors.newSingleThreadExecutor()

            // Unbind previous use cases (e.g. the old analyzer)
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = view.surfaceProvider
            }

            // Switch Analyzer based on Mode
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
                camera = provider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
                // Restore torch state after re-binding
                onTorchToggle(camera?.cameraInfo?.hasFlashUnit() ?: false)
                camera?.cameraControl?.enableTorch(torchEnabled)
            } catch (_: Exception) {
                // Log error
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCamPermission) {
            // REMOVED: key(scanMode) wrapper
            // ADDED: <PreviewView> explicit type
            AndroidView<PreviewView>(
                factory = { ctx ->
                    val view = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                    previewView = view // Capture for LaunchedEffect

                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        try {
                            cameraProvider = providerFuture.get()
                        } catch (_: Exception) {
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    view
                },
                modifier = Modifier.fillMaxSize()
            )

            // Visual Overlay
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
