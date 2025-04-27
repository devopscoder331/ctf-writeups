package app.blackhol3.ui.keygen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.blackhol3.model.PrivateKey
import app.blackhol3.service.EntropyCollectionStatus
import app.blackhol3.service.KeyGenerationService
import app.blackhol3.service.KeyGenerationState
import app.blackhol3.service.SensorSample
import app.blackhol3.ui.common.PrivateKeyViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KeyGenerationViewModel(
    private val svc: KeyGenerationService,
    private val privateKeyViewModel: PrivateKeyViewModel,
) : ViewModel() {
    val uiState: StateFlow<KeyGenerationUIState> =
        svc.stateFlow
            .map { it.toUiState() }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                KeyGenerationUIState(),
            )

    val entropyBytesFlow = svc.entropyBytesFlow

    fun registerSensorSampleFlow(sampleFlow: Flow<SensorSample>) {
        svc.registerSensorSampleFlow(sampleFlow)
    }

    fun startCollection() {
        svc.startCollection()
    }

    fun captureSensorData(
        clickX: Float,
        clickY: Float,
    ) {
        svc.captureSensorData(clickX, clickY)
    }

    fun saveCurrentKey(replaceId: String? = null) {
        viewModelScope.launch {
            val keyWithStrength = svc.getCurrentKeyWithStrength() ?: return@launch

            val (privateKey, _) = keyWithStrength

            if (replaceId != null) {
                privateKeyViewModel.replacePrivateKey(replaceId, privateKey)
            } else {
                privateKeyViewModel.setNewPrivateKey(privateKey)
            }

            svc.markKeySaved()
        }
    }

    fun reset() {
        svc.reset()
    }

    private fun KeyGenerationState.toUiState(): KeyGenerationUIState =
        KeyGenerationUIState(
            isCollecting = this.isCollecting,
            isGeneratingKey = this.keyState.isGenerating,
            generatingKeyStrength = this.keyState.targetKeyStrength,
            entropyCollected = this.entropyState.currentBytes,
            maxEntropyNeeded = this.entropyState.maxBytes,
            entropyPercentage = this.entropyState.percentage,
            currentKeyStrength = this.keyState.keyStrength,
            currentKey = this.keyState.currentKey,
            keyGenerated = this.keyState.currentKey != null,
            keySaved = this.keySaved,
            status = this.status,
        )

    override fun onCleared() {
        super.onCleared()
        svc.cleanup()
    }
}

data class KeyGenerationUIState(
    val isCollecting: Boolean = false,
    val isGeneratingKey: Boolean = false,
    val generatingKeyStrength: Int = 0,
    val entropyCollected: Int = 0,
    val maxEntropyNeeded: Int = KeyGenerationService.MAX_ENTROPY_BYTES,
    val entropyPercentage: Float = 0f,
    val currentKeyStrength: Int = 0,
    val currentKey: PrivateKey? = null,
    val keyGenerated: Boolean = false,
    val keySaved: Boolean = false,
    val status: EntropyCollectionStatus = EntropyCollectionStatus.Ready,
)
