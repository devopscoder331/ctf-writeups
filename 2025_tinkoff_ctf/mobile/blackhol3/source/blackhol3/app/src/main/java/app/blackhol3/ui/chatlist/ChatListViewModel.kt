package app.blackhol3.ui.chatlist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.blackhol3.data.local.model.Chat
import app.blackhol3.model.Message
import app.blackhol3.model.PublicKey
import app.blackhol3.repository.ChatRepository
import app.blackhol3.repository.MessagesRepository
import app.blackhol3.ui.common.PrivateKeyViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class ChatsUiState {
    data object Loading : ChatsUiState()

    data class Success(
        val chats: List<Chat>,
    ) : ChatsUiState()

    data object Error : ChatsUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessagesRepository,
    private val privateKeyViewModel: PrivateKeyViewModel,
) : ViewModel() {
    val currentPrivateKey = privateKeyViewModel.currentPrivateKey

    private val latestMessageFeeds = mutableMapOf<String, Flow<Message?>>()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiState = MutableStateFlow<ChatsUiState>(ChatsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentPrivateKey
                .filterNotNull()
                .flatMapLatest { privateKey ->
                    chatRepository
                        .getChats(privateKey)
                        .map { ChatsUiState.Success(it) as ChatsUiState }
                        .catch { emit(ChatsUiState.Error) }
                }.collect { _uiState.value = it }
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            currentPrivateKey.value?.let { privateKey ->
                try {
                    _isRefreshing.value = true
                    chatRepository.refreshChats(privateKey)
                    delay(800)
                } catch (e: Exception) {
                } finally {
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            currentPrivateKey.value?.let { privateKey ->
                chatRepository.deleteChat(privateKey, chatId)
            }
        }
    }

    fun renameChat(
        chatId: String,
        newName: String,
    ) {
        viewModelScope.launch {
            currentPrivateKey.value?.let { privateKey ->
                chatRepository.renameChat(privateKey, chatId, newName)
            }
        }
    }

    fun addChat(
        pubKey: PublicKey,
        name: String? = null,
    ) {
        viewModelScope.launch {
            currentPrivateKey.value?.let { privateKey ->
                chatRepository.addChat(privateKey, pubKey, name)
            }
        }
    }

    fun latestMessageFeed(chatId: String): Flow<Message?> =
        latestMessageFeeds.getOrPut(chatId) {
            val newFlow = MutableStateFlow<Message?>(null)
            viewModelScope.launch {
                currentPrivateKey
                    .filterNotNull()
                    .flatMapLatest { privateKey ->
                        messageRepository.lastMessageFeed(privateKey, chatId)
                    }.collect { newFlow.value = it }
            }
            newFlow
        }

    fun sharePublicKey(context: Context) {
        privateKeyViewModel.sharePublicKey(context)
    }

    override fun onCleared() {
        super.onCleared()
        latestMessageFeeds.clear()
    }
}
