package app.blackhol3.ui.keygen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.blackhol3.ByteGridBackground
import app.blackhol3.service.EntropyCollectionStatus
import app.blackhol3.service.SensorSample
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.androidx.compose.koinViewModel

const val backgroundAlpha = 0.75f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyGenerationScreen(
    viewModel: KeyGenerationViewModel = koinViewModel(),
    regeneratedKeyId: String? = null,
    onNavigateBack: () -> Unit,
    onKeySaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var clickPosition by remember { mutableStateOf(Offset.Zero) }

    SetupSensorInput(viewModel)

    LaunchedEffect(uiState.keySaved) {
        if (uiState.keySaved) {
            onKeySaved()
        }
    }

    val title =
        when {
            uiState.isGeneratingKey ->
                "Генерируем ключ ${uiState.generatingKeyStrength} ${pluralizeBits(uiState.generatingKeyStrength)}..."

            uiState.currentKeyStrength > 0 ->
                "Размер ключа: ${uiState.currentKeyStrength} ${pluralizeBits(uiState.currentKeyStrength)}"

            uiState.isCollecting ->
                "Собираем энтропию..."

            else ->
                "Генерация ключа"
        }

    val statusMessage = getStatusMessage(uiState.status)

    ByteGridBackground(byteFlow = viewModel.entropyBytesFlow) { resetBackground ->
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.entropyCollected} из ${uiState.maxEntropyNeeded} байт энтропии собрано",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    colors =
                        androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background.copy(alpha = backgroundAlpha),
                        ),
                )
            },
            containerColor = Color.Transparent,
        ) { paddingValues ->
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = backgroundAlpha),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(paddingValues),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { uiState.entropyPercentage / 100f },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(6.dp)),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getStatusColor(uiState.status),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = paddingValues.calculateBottomPadding()),
                ) {
                    CollectionButton(
                        isCollecting = uiState.isCollecting,
                        onStartCollection = viewModel::startCollection,
                        onCaptureData = {
                            viewModel.captureSensorData(
                                clickPosition.x,
                                clickPosition.y,
                            )
                        },
                        onPositionChange = { clickPosition = it },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                ControlButtonsSection(
                    keyGenerated = uiState.keyGenerated,
                    isGeneratingKey = uiState.isGeneratingKey,
                    onReset = {
                        viewModel.reset()
                        resetBackground()
                    },
                    onSaveKey = { viewModel.saveCurrentKey(regeneratedKeyId) },
                    modifier = Modifier.align(Alignment.End),
                    paddingValues = paddingValues,
                )
            }
        }
    }
}

@Composable
private fun SetupSensorInput(viewModel: KeyGenerationViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) ?: return@LaunchedEffect

        val sampleFlow =
            MutableSharedFlow<SensorSample>(
                extraBufferCapacity = 4,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

        val listener =
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    sampleFlow.tryEmit(SensorSample.fromSensorEvent(event))
                }

                override fun onAccuracyChanged(
                    sensor: Sensor,
                    accuracy: Int,
                ) {}
            }

        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_GAME,
        )

        viewModel.registerSensorSampleFlow(sampleFlow)

        try {
            awaitCancellation()
        } finally {
            sensorManager.unregisterListener(listener)
        }
    }
}

@Composable
private fun CollectionButton(
    isCollecting: Boolean,
    onStartCollection: () -> Unit,
    onCaptureData: () -> Unit,
    onPositionChange: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = {
            if (isCollecting) {
                onCaptureData()
            } else {
                onStartCollection()
            }
        },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
            ),
        modifier =
            modifier
                .size(200.dp)
                .clip(CircleShape)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val position = event.changes.first().position
                            onPositionChange(position)
                        }
                    }
                },
    ) {
        Text(
            text = if (isCollecting) "Собрать" else "Начать",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ControlButtonsSection(
    keyGenerated: Boolean,
    isGeneratingKey: Boolean,
    onReset: () -> Unit,
    onSaveKey: () -> Unit,
    modifier: Modifier,
    paddingValues: PaddingValues,
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = backgroundAlpha),
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .padding(bottom = 24.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Button(
                    onClick = onReset,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ),
                    modifier = Modifier.weight(0.7f),
                ) {
                    Text("Сбросить")
                }

                Spacer(modifier = Modifier.weight(0.1f))

                Button(
                    onClick = onSaveKey,
                    enabled = keyGenerated && !isGeneratingKey,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Сохранить ключ")
                }
            }
        }
    }
}

private fun getStatusMessage(status: EntropyCollectionStatus): String =
    when (status) {
        is EntropyCollectionStatus.Added -> "Энтропия добавлена. Повторите нажатие."
        is EntropyCollectionStatus.Awaiting -> "Ожидание энтропии"
        is EntropyCollectionStatus.NotEnoughMovement -> "Недостаточно случайное движение!"
        is EntropyCollectionStatus.Ready -> "Возьмите телефон в лапку и нажимайте на кнопку"
        is EntropyCollectionStatus.KeyReady -> "Можно сохранить ключ"
    }

@Composable
private fun getStatusColor(status: EntropyCollectionStatus) =
    when (status) {
        is EntropyCollectionStatus.NotEnoughMovement -> MaterialTheme.colorScheme.error
        is EntropyCollectionStatus.Added -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

private fun pluralizeBits(count: Int): String =
    when {
        count % 10 == 1 && count % 100 != 11 -> "бит"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "бита"
        else -> "бит"
    }
