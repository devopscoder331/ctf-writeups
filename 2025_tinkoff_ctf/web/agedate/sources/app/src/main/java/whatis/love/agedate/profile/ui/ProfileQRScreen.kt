package whatis.love.agedate.profile.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import whatis.love.agedate.profile.viewmodel.ProfileQRScreenViewModel
import whatis.love.agedate.qrcode.generateProfileQRCode
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileQRScreen(
    viewModel: ProfileQRScreenViewModel,
    appPackageName: String,
    backgroundGradientColors: List<Color> =
        listOf(
            Color(0xFFFFAF8C),
            Color(0xFFFFD0B5),
            Color(0xFFFFF0E5),
        ),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile
    val qrBitmap =
        remember(profile) {
            profile?.let { generateProfileQRCode(context, it) }
        }
    val userName = profile?.let { "${it.firstName} ${it.lastName}" } ?: ""

    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val animateIn = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateIn.value = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR-код") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors = backgroundGradientColors,
                            ),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            } else if (uiState.errorMessage != null) {
                Card(
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp)),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier =
                            Modifier
                                .padding(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Ошибка",
                            tint = MaterialTheme.colorScheme.error,
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .padding(bottom = 16.dp),
                        )

                        uiState.errorHeader?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = uiState.errorMessage ?: "Произошла ошибка",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.refresh() },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Повторить",
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Повторить")
                        }
                    }
                }
            } else if (profile != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .padding(paddingValues)
                            .padding(16.dp),
                ) {
                    AnimatedVisibility(
                        visible = animateIn.value,
                        enter =
                            fadeIn(tween(durationMillis = 500, easing = Ease)) +
                                slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = spring(0.5f, 600f),
                                ),
                    ) {
                        Card(
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp)),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier =
                                    Modifier
                                        .background(
                                            brush =
                                                Brush.verticalGradient(
                                                    colors =
                                                        listOf(
                                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                                alpha = 0.3f,
                                                            ),
                                                            MaterialTheme.colorScheme.surface,
                                                        ),
                                                ),
                                        ).padding(24.dp),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .padding(bottom = 16.dp)
                                            .size(250.dp)
                                            .shadow(4.dp, RoundedCornerShape(16.dp))
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    qrBitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "QR-код пользователя",
                                            modifier = Modifier.size(220.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Покажите или отправьте этот код тому, с кем хотите поделиться профилем",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val success =
                                        saveQRCodeToGallery(
                                            context = context,
                                            qrBitmap = qrBitmap,
                                            userName = userName,
                                        )
                                    snackbarMessage =
                                        if (success) {
                                            "QR-код сохранён в галерею"
                                        } else {
                                            "Не удалось сохранить QR-код"
                                        }
                                    snackbarHostState.showSnackbar(snackbarMessage)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            enabled = qrBitmap != null,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Сохранить QR-код",
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Сохранить")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    val uri =
                                        shareQRCode(
                                            context = context,
                                            qrBitmap = qrBitmap,
                                            userName = userName,
                                            appPackageName = appPackageName,
                                        )
                                    if (uri != null) {
                                        val shareIntent =
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "image/png"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Отправить QR-код"),
                                            null,
                                        )
                                    } else {
                                        snackbarMessage = "Не удалось отправить QR-код"
                                        snackbarHostState.showSnackbar(snackbarMessage)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                ),
                            enabled = qrBitmap != null,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Отправить QR-код",
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Отправить")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

fun createFinalBitmap(
    qrBitmap: Bitmap,
    userName: String,
): Bitmap {
    val margin = 100
    val textHeight = 50
    val width = qrBitmap.width + (margin * 2)
    val height = qrBitmap.height + (margin * 2) + textHeight

    val finalBitmap = createBitmap(width, height)
    val canvas = Canvas(finalBitmap)
    val paint =
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.White.toArgb()
        }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    canvas.drawBitmap(qrBitmap, margin.toFloat(), margin.toFloat(), null)
    paint.apply {
        color = Color.Black.toArgb()
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(
        userName,
        (width / 2).toFloat(),
        (qrBitmap.height + margin + 40).toFloat(),
        paint,
    )

    return finalBitmap
}

private suspend fun saveQRCodeToGallery(
    context: Context,
    qrBitmap: Bitmap?,
    userName: String,
): Boolean =
    withContext(Dispatchers.IO) {
        if (qrBitmap == null) return@withContext false
        val finalBitmap = createFinalBitmap(qrBitmap, userName)

        try {
            val fileName = "AgeDate_$userName.png"
            var outputStream: OutputStream? = null
            var uri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues =
                    ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                context.contentResolver.run {
                    uri = insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    uri?.let { outputStream = openOutputStream(it) }
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(imagesDir, fileName)
                outputStream = FileOutputStream(imageFile)
                uri = Uri.fromFile(imageFile)
            }

            if (outputStream != null) {
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
                outputStream?.close()
                return@withContext true
            }

            return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

private suspend fun shareQRCode(
    context: Context,
    qrBitmap: Bitmap?,
    userName: String,
    appPackageName: String,
): Uri? =
    withContext(Dispatchers.IO) {
        if (qrBitmap == null) return@withContext null

        try {
            val finalBitmap = createFinalBitmap(qrBitmap, userName)
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()

            val fileName = "AgeDate_$userName.png"
            val file = File(cachePath, fileName)

            FileOutputStream(file).use { outputStream ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
            }

            return@withContext FileProvider.getUriForFile(
                context,
                "$appPackageName.fileprovider",
                file,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
