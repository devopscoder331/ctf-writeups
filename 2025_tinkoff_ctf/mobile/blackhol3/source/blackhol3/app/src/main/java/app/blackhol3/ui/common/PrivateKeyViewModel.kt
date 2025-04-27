package app.blackhol3.ui.common

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.blackhol3.model.PrivateKey
import app.blackhol3.provider.BlobContentProvider
import app.blackhol3.repository.PrivateKeyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrivateKeyViewModel(
    private val privateKeyRepository: PrivateKeyRepository,
) : ViewModel() {
    private val _currentPrivateKey = MutableStateFlow<PrivateKey?>(null)
    val currentPrivateKey = _currentPrivateKey.asStateFlow()

    private val _privateKeys = MutableStateFlow<List<PrivateKey>>(emptyList())
    val privateKeys = _privateKeys.asStateFlow()

    private val _currentState =
        MutableStateFlow<PrivateKeyViewModelState>(PrivateKeyViewModelState.Loading)
    val currentState = _currentState.asStateFlow()

    init {
        viewModelScope.launch {
            privateKeyRepository
                .privateKeys()
                .collect { _privateKeys.value = it }
        }

        viewModelScope.launch {
            privateKeyRepository
                .currentPrivateKey()
                .collect {
                    _currentPrivateKey.value = it
                    _currentState.value = PrivateKeyViewModelState.Done
                }
        }
    }

    fun choosePrivateKey(privateKey: PrivateKey) {
        choosePrivateKey(privateKey.id)
    }

    fun choosePrivateKey(id: String) {
        privateKeyRepository.setCurrentPrivateKey(id)
    }

    fun setNewPrivateKey(privateKey: PrivateKey) {
        viewModelScope.launch {
            privateKeyRepository.insertPrivateKey(privateKey)
            choosePrivateKey(privateKey)
        }
    }

    fun replacePrivateKey(
        id: String,
        privateKey: PrivateKey,
    ) {
        viewModelScope.launch {
            val key = privateKeyRepository.replacePrivateKey(id, privateKey)
            choosePrivateKey(key)
        }
    }

    fun sharePublicKey(
        context: Context,
        key: PrivateKey,
    ) {
        viewModelScope.launch {
            val uri = BlobContentProvider.getPubKeyUri(key.id)
            val sendIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/x-pem-file"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            val shareIntent = Intent.createChooser(sendIntent, "Отправить публичный ключ")
            context.startActivity(shareIntent)
        }
    }

    fun sharePublicKey(context: Context) {
        viewModelScope.launch {
            sharePublicKey(context, currentPrivateKey.value!!)
        }
    }

    fun deletePrivateKey(id: String) {
        viewModelScope.launch {
            privateKeyRepository.deletePrivateKey(id)
        }
    }
}

sealed class PrivateKeyViewModelState {
    object Loading : PrivateKeyViewModelState()

    object Done : PrivateKeyViewModelState()
}
