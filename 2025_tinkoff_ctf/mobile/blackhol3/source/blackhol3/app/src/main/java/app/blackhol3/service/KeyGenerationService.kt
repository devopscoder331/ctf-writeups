package app.blackhol3.service

import android.hardware.SensorEvent
import android.util.Log
import app.blackhol3.model.PrivateKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.RSAKeyGenParameterSpec
import java.util.UUID
import kotlin.experimental.xor
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

class KeyGenerationService(
    private val keyPicGenerationService: KeyPicGenerationService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val publicExponent = RSAKeyGenParameterSpec.F4

    private val _stateFlow = MutableStateFlow(KeyGenerationState())
    val stateFlow = _stateFlow.asStateFlow()

    private val _entropyBytesFlow = MutableSharedFlow<Byte?>(extraBufferCapacity = 4)
    val entropyBytesFlow = _entropyBytesFlow.asSharedFlow()

    private val entropyPool = ByteArray(MAX_ENTROPY_BYTES)
    private val sha256Digest = MessageDigest.getInstance("SHA-256")
    private val previousHashes = ArrayDeque<ByteArray>(20)

    private var sensorSampleFlow: Flow<SensorSample>? = null
    private var sensorCollectionJob: Job? = null

    companion object {
        const val MAX_ENTROPY_BYTES = 4096
        const val ENTROPY_THRESHOLD_1024 = 256
        const val ENTROPY_THRESHOLD_2048 = 1024
        const val ENTROPY_THRESHOLD_3072 = 2048
        const val ENTROPY_THRESHOLD_4096 = 4096

        private const val SENSOR_COLLECTION_TIME_MS = 25L
        private const val MINIMUM_MOVEMENT_THRESHOLD = 1.6f
    }

    fun registerSensorSampleFlow(flow: Flow<SensorSample>) {
        sensorSampleFlow = flow
    }

    fun startCollection() {
        _stateFlow.update { currentState ->
            currentState.copy(
                isCollecting = true,
                status = EntropyCollectionStatus.Awaiting,
            )
        }
    }

    fun stopCollection(): PrivateKey? {
        sensorCollectionJob?.cancel()
        val currentKey = _stateFlow.value.keyState.currentKey

        _stateFlow.update { currentState ->
            currentState.copy(
                isCollecting = false,
                status =
                    if (currentKey != null) {
                        EntropyCollectionStatus.KeyReady
                    } else {
                        EntropyCollectionStatus.Ready
                    },
            )
        }

        return currentKey
    }

    fun captureSensorData(
        clickX: Float,
        clickY: Float,
    ) {
        val currentState = _stateFlow.value
        if (!currentState.isCollecting ||
            currentState.keyState.isGenerating
        ) {
            return
        }

        sensorCollectionJob?.cancel()

        sensorCollectionJob =
            CoroutineScope(dispatcher).launch {
                val start = System.currentTimeMillis()
                val samples =
                    sensorSampleFlow!!
                        .takeWhile {
                            System.currentTimeMillis() - start < SENSOR_COLLECTION_TIME_MS
                        }.toList()

                val totalMovement =
                    samples.sumOf { (abs(it.x) + abs(it.y) + abs(it.z)).toDouble() }.toFloat()
                val averageMovement = if (samples.isNotEmpty()) totalMovement / samples.size else 0f
                val enoughMovement = averageMovement >= MINIMUM_MOVEMENT_THRESHOLD
                val trustBytes = (minOf(maxOf(averageMovement, 4f), 16f) / 2).roundToInt() * 2
                Log.e("KeyGenerationService", "Movement: $averageMovement")
                Log.e("KeyGenerationService", "Trust bytes: $trustBytes")

                val entropyAdded =
                    if (enoughMovement && samples.isNotEmpty()) {
                        samples.count { sample ->
                            val filteredX = sample.x * System.nanoTime() % 10
                            val filteredY = sample.y * (System.currentTimeMillis() % 100) / 10
                            val filteredZ = sample.z + sin(System.nanoTime().toDouble()).toFloat()

                            addSensorEntropy(
                                x = filteredX,
                                y = filteredY,
                                z = filteredZ,
                                timestamp = sample.timestamp,
                                clickX = clickX,
                                clickY = clickY,
                                sensorBytes = trustBytes,
                            )
                        }
                    } else {
                        if (addSensorEntropy(
                                x = 0f,
                                y = 0f,
                                z = 0f,
                                timestamp = System.currentTimeMillis(),
                                clickX = clickX,
                                clickY = clickY,
                                sensorBytes = 0,
                            )
                        ) {
                            1
                        } else {
                            0
                        }
                    }

                _stateFlow.update { state ->
                    state.copy(
                        status =
                            if (enoughMovement && entropyAdded > 0) {
                                EntropyCollectionStatus.Added
                            } else {
                                EntropyCollectionStatus.NotEnoughMovement
                            },
                    )
                }
            }
    }

    private fun addSensorEntropy(
        x: Float,
        y: Float,
        z: Float,
        timestamp: Long,
        clickX: Float,
        clickY: Float,
        sensorBytes: Int,
    ): Boolean {
        if (!_stateFlow.value.isCollecting) return false

        val clickData =
            ByteBuffer
                .allocate(16)
                .putFloat(clickX)
                .putFloat(clickY)
                .putLong(System.nanoTime())
                .array()
        val clickHash = sha256Digest.digest(clickData).sliceArray(0..3)

        val movementMagnitude = abs(x) + abs(y) + abs(z)
        if (movementMagnitude < 0.2f) {
            return addEntropyWithMixing(clickHash, 4)
        }

        val sensorData =
            ByteBuffer
                .allocate(28)
                .putFloat(x)
                .putFloat(y)
                .putFloat(z)
                .putLong(timestamp)
                .putLong(System.nanoTime())
                .array()

        val sensorHash = sha256Digest.digest(sensorData)

        if (isTooSimilarToRecent(sensorHash, previousHashes)) {
            return addEntropyWithMixing(clickHash, 4)
        }

        if (previousHashes.size >= 20) {
            previousHashes.removeFirst()
        }
        previousHashes.addLast(sensorHash)

        return addEntropyWithMixing(sensorHash, sensorBytes)
    }

    private fun isTooSimilarToRecent(
        hash: ByteArray,
        recentHashes: Collection<ByteArray>,
    ): Boolean {
        for (recentHash in recentHashes) {
            var differentBits = 0
            for (i in hash.indices) {
                val xorResult = hash[i].toInt() xor recentHash[i].toInt()
                differentBits += Integer.bitCount(xorResult)
            }

            if (differentBits < (hash.size * 8 * 0.25)) {
                return true
            }
        }
        return false
    }

    private fun addEntropyWithMixing(
        dataHash: ByteArray,
        maxBytes: Int,
    ): Boolean {
        val currentEntropyBytes = _stateFlow.value.entropyState.currentBytes

        if (currentEntropyBytes >= MAX_ENTROPY_BYTES) return true

        val bytesToAdd = minOf(maxBytes, MAX_ENTROPY_BYTES - currentEntropyBytes)
        if (bytesToAdd <= 0) return false

        var addedBytes = 0

        for (i in 0 until bytesToAdd) {
            val sourceIndex = i % dataHash.size
            val targetIndex = currentEntropyBytes + addedBytes

            val foldIndex = (dataHash.size - 1 - sourceIndex) % dataHash.size

            val resultingByte = (
                (entropyPool[targetIndex] xor dataHash[sourceIndex]) xor
                    (dataHash[foldIndex] xor (System.nanoTime() % 256).toByte())
            )
            entropyPool[targetIndex] = resultingByte
            _entropyBytesFlow.tryEmit(resultingByte)
            addedBytes++
        }

        val newTotalEntropy = currentEntropyBytes + addedBytes
        _stateFlow.update { state ->
            state.copy(
                entropyState =
                    state.entropyState.copy(
                        currentBytes = newTotalEntropy,
                    ),
            )
        }

        checkAndGenerateKey()

        return true
    }

    private fun determineKeyStrength(): Int {
        val currentEntropyBytes = _stateFlow.value.entropyState.currentBytes
        return when {
            currentEntropyBytes >= ENTROPY_THRESHOLD_4096 -> 4096
            currentEntropyBytes >= ENTROPY_THRESHOLD_3072 -> 3072
            currentEntropyBytes >= ENTROPY_THRESHOLD_2048 -> 2048
            currentEntropyBytes >= ENTROPY_THRESHOLD_1024 -> 1024
            else -> 0
        }
    }

    private fun checkAndGenerateKey() {
        val currentState = _stateFlow.value
        val currentKeyStrength = currentState.keyState.keyStrength
        val newKeyStrength = determineKeyStrength()

        if (newKeyStrength > currentKeyStrength && !currentState.keyState.isGenerating) {
            _stateFlow.update { state ->
                state.copy(
                    keyState =
                        state.keyState.copy(
                            isGenerating = true,
                            targetKeyStrength = newKeyStrength,
                        ),
                )
            }

            CoroutineScope(dispatcher).launch(dispatcher) {
                val newKey =
                    withContext(dispatcher) {
                        generate(newKeyStrength, entropyPool.copyOf())
                    }

                _stateFlow.update { state ->
                    state.copy(
                        keyState =
                            state.keyState.copy(
                                currentKey = newKey,
                                keyStrength = newKeyStrength,
                                isGenerating = false,
                                targetKeyStrength = 0,
                            ),
                    )
                }
            }
        }
    }

    private fun generate(
        keySize: Int,
        pool: ByteArray,
    ): PrivateKey {
        val entropyHash = sha256Digest.digest(pool)
        val secureRandom = SecureRandom().apply { setSeed(entropyHash) }
        val rsaKeyGen = KeyPairGenerator.getInstance("RSA")
        rsaKeyGen.initialize(
            RSAKeyGenParameterSpec(
                keySize,
                publicExponent,
            ),
            secureRandom,
        )

        val keyPair = rsaKeyGen.generateKeyPair()
        val name = UUID.randomUUID().toString()
        return PrivateKey(
            name,
            keyPair.private.encoded,
            keyPicGenerationService,
        )
    }

    fun getCurrentKeyWithStrength(): Pair<PrivateKey, Int>? {
        val currentState = _stateFlow.value
        val key = currentState.keyState.currentKey ?: return null
        return Pair(key, currentState.keyState.keyStrength)
    }

    fun cleanup() {
        sensorCollectionJob?.cancel()
    }

    fun reset() {
        stopCollection()
        sensorCollectionJob?.cancel()
        entropyPool.fill(0)
        previousHashes.clear()

        _stateFlow.value = KeyGenerationState()
    }

    fun markKeySaved() {
        _stateFlow.update { it.copy(keySaved = true) }
    }
}

sealed class EntropyCollectionStatus {
    object Ready : EntropyCollectionStatus()

    object Awaiting : EntropyCollectionStatus()

    object NotEnoughMovement : EntropyCollectionStatus()

    object Added : EntropyCollectionStatus()

    object KeyReady : EntropyCollectionStatus()
}

data class EntropyState(
    val currentBytes: Int = 0,
    val maxBytes: Int = KeyGenerationService.MAX_ENTROPY_BYTES,
) {
    val percentage by lazy {
        if (maxBytes > 0) (currentBytes.toFloat() / maxBytes) * 100f else 0f
    }
}

data class KeyState(
    val currentKey: PrivateKey? = null,
    val keyStrength: Int = 0,
    val isGenerating: Boolean = false,
    val targetKeyStrength: Int = 0,
)

data class KeyGenerationState(
    val entropyState: EntropyState = EntropyState(),
    val keyState: KeyState = KeyState(),
    val isCollecting: Boolean = false,
    val status: EntropyCollectionStatus = EntropyCollectionStatus.Ready,
    val keySaved: Boolean = false,
)

data class SensorSample(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long,
) {
    companion object {
        fun fromSensorEvent(event: SensorEvent): SensorSample =
            SensorSample(
                x = event.values[0],
                y = event.values[1],
                z = event.values[2],
                timestamp = event.timestamp,
            )
    }
}
