package whatis.love.agedate.qrcode.ui

import android.Manifest
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import whatis.love.agedate.qrcode.ZXingAnalyzer
import java.util.concurrent.Executors
import kotlin.text.Typography.nbsp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    modifier: Modifier = Modifier,
    validCodePredicate: (String) -> Boolean = { true },
    onNavigateBack: () -> Unit,
    onQRCodeScanned: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    var hasCameraPermission by remember { mutableStateOf(false) }
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasCameraPermission = granted
            },
        )

    LaunchedEffect(key1 = true) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var scanned by remember { mutableStateOf(false) }

    fun handleQRCodeScanned(code: String) {
        if (!scanned) {
            scanned = true
            mainExecutor.execute {
                onQRCodeScanned(code)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    previewView.apply {
                        layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = modifier.fillMaxSize(),
                update = { view ->
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview =
                            Preview.Builder().build().also {
                                it.surfaceProvider = view.surfaceProvider
                            }

                        val imageAnalyzer =
                            ImageAnalysis
                                .Builder()
                                .build()
                                .also {
                                    it.setAnalyzer(
                                        Executors.newSingleThreadExecutor(),
                                        ZXingAnalyzer(
                                            validCodePredicate = validCodePredicate,
                                            onQRCodeScanned = { handleQRCodeScanned(it) },
                                        ),
                                    )
                                }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalyzer,
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, mainExecutor)
                },
            )
            QRCodeScannerOverlay()
            val scannerSize = with(LocalDensity.current) { 280.dp.toPx() }
            val density = LocalDensity.current

            Box(
                modifier =
                    Modifier
                        .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(with(density) { scannerSize.toDp() }),
                ) {}
                Text(
                    text = "Поместите${nbsp}QR-код в${nbsp}рамку для${nbsp}сканирования",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .padding(horizontal = 32.dp)
                            .offset(y = with(density) { (scannerSize / 2 + 90).toDp() }),
                )
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    text = "Для сканирования QR-кодов\nнужен доступ к камере",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
                Button(
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                ) {
                    Text("Разрешить")
                }
            }
        }
        TopAppBar(
            title = { Text("Сканер QR-кода", color = Color.White.copy(alpha = 0.8f)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White.copy(alpha = 0.8f),
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.2f),
                ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun QRCodeScannerOverlay() {
    val scannerSize = with(LocalDensity.current) { 280.dp.toPx() }
    val cornerLength = with(LocalDensity.current) { 40.dp.toPx() }
    val cornerThickness = with(LocalDensity.current) { 6.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val scannerRect =
            Rect(
                left = centerX - scannerSize / 2,
                top = centerY - scannerSize / 2,
                right = centerX + scannerSize / 2,
                bottom = centerY + scannerSize / 2,
            )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, 0f),
            size = Size(size.width, scannerRect.top),
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, scannerRect.bottom),
            size = Size(size.width, size.height - scannerRect.bottom),
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, scannerRect.top),
            size = Size(scannerRect.left, scannerRect.height),
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(scannerRect.right, scannerRect.top),
            size = Size(size.width - scannerRect.right, scannerRect.height),
        )
        drawLine(
            color = Color.White,
            start = Offset(scannerRect.left, scannerRect.top + cornerLength),
            end = Offset(scannerRect.left, scannerRect.top),
            strokeWidth = cornerThickness,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(scannerRect.left, scannerRect.top),
            end = Offset(scannerRect.left + cornerLength, scannerRect.top),
            strokeWidth = cornerThickness,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(scannerRect.right - cornerLength, scannerRect.top),
            end = Offset(scannerRect.right, scannerRect.top),
            strokeWidth = cornerThickness,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(scannerRect.right, scannerRect.top),
            end = Offset(scannerRect.right, scannerRect.top + cornerLength),
            strokeWidth = cornerThickness,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(scannerRect.left, scannerRect.bottom - cornerLength),
            end = Offset(scannerRect.left, scannerRect.bottom),
            strokeWidth = cornerThickness,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(scannerRect.left, scannerRect.bottom),
            end = Offset(scannerRect.left + cornerLength, scannerRect.bottom),
            strokeWidth = cornerThickness,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(scannerRect.right - cornerLength, scannerRect.bottom),
            end = Offset(scannerRect.right, scannerRect.bottom),
            strokeWidth = cornerThickness,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(scannerRect.right, scannerRect.bottom),
            end = Offset(scannerRect.right, scannerRect.bottom - cornerLength),
            strokeWidth = cornerThickness,
            cap = StrokeCap.Round,
        )
    }
}
