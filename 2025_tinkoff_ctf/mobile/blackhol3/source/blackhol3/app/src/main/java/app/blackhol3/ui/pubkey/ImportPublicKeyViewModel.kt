package app.blackhol3.ui.pubkey

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.blackhol3.model.PublicKey
import app.blackhol3.service.KeyPicGenerationService
import app.blackhol3.ui.chatlist.ChatListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class ImportPublicKeyUiState(
    val pastedText: String = "",
    val pastedTextError: String? = null,
    val pubkey: PublicKey? = null,
    val isKeyValid: Boolean = false,
)

class ImportPublicKeyViewModel(
    private val chatListViewModel: ChatListViewModel,
    private val keyPicGenerator: KeyPicGenerationService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ImportPublicKeyUiState())
    val uiState: StateFlow<ImportPublicKeyUiState> = _uiState.asStateFlow()

    fun updatePastedText(text: String) {
        _uiState.update {
            it.copy(
                pastedText = text,
                pastedTextError = null,
            )
        }
    }

    fun processFileUri(
        context: Context,
        uri: Uri,
    ): Boolean =
        try {
            val inputStream = context.contentResolver.openInputStream(uri)

            inputStream?.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                val pemContent = reader.readLines().joinToString("\n")
                tryAddKey(pemContent)
            } == true
        } catch (e: Exception) {
            false
        }

    fun processPastedText(): Boolean {
        val text = _uiState.value.pastedText.trim()

        if (text.isEmpty()) {
            _uiState.update {
                it.copy(
                    pastedTextError = "Поле не может быть пустым",
                )
            }
            return false
        }

        val result = tryAddKey(text)
        if (!result) {
            _uiState.update {
                it.copy(
                    pastedTextError = "Неверный ключ",
                )
            }
        }
        return result
    }

    fun tryAddKey(key: String): Boolean {
        val keyBytes = extractPemBytes(key) ?: return false

        if (keyBytes.isEmpty()) {
            return false
        }

        val pubKey: PublicKey
        try {
            pubKey = PublicKey(keyBytes = keyBytes, keyPicGenerator = keyPicGenerator)
        } catch (e: Exception) {
            return false
        }

        _uiState.update {
            it.copy(
                pubkey = pubKey,
                isKeyValid = true,
                pastedTextError = null,
            )
        }
        return true
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun extractPemBytes(pemContent: String): ByteArray? {
        val beginMarker = "-----BEGIN"
        val endMarker = "-----END"

        if (!pemContent.contains(beginMarker) || !pemContent.contains(endMarker)) {
            return null
        }

        try {
            val lines = pemContent.lines()
            val base64Lines =
                lines.filter {
                    !it.contains(beginMarker) &&
                        !it.contains(endMarker) &&
                        it.trim().isNotEmpty()
                }

            val base64Content = base64Lines.joinToString("")
            return Base64.decode(base64Content)
        } catch (e: Exception) {
            return null
        }
    }

    fun resetKey() {
        _uiState.update {
            ImportPublicKeyUiState(pastedText = "")
        }
    }

    fun addKey(name: String) {
        viewModelScope.launch {
            val pubKey = _uiState.value.pubkey ?: return@launch
            chatListViewModel.addChat(pubKey, name)
        }
    }
}
