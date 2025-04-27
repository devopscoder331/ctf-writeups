package app.blackhol3.ui.chat

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.blackhol3.data.local.model.Chat
import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Media
import app.blackhol3.model.Message
import app.blackhol3.model.MessageEvent
import app.blackhol3.provider.BlobContentProvider
import app.blackhol3.repository.ChatRepository
import app.blackhol3.repository.ContentProviderRepository
import app.blackhol3.repository.MessagesRepository
import app.blackhol3.service.MessagePollService
import app.blackhol3.ui.common.PrivateKeyViewModel
import app.blackhol3.util.FileUtil
import app.blackhol3.util.toMessengerFormattedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val chatId: String,
    private val privateKeyViewModel: PrivateKeyViewModel,
    private val chatRepository: ChatRepository,
    private val messagesRepository: MessagesRepository,
    private val contentProviderRepository: ContentProviderRepository,
    private val messageUpdateService: MessagePollService,
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat = _chat.asStateFlow()

    private val _messageDeliveryStatusMap =
        MutableStateFlow<Map<String, DeliveryStatus>>(emptyMap())

    val messagesWithStatus =
        combine(
            messages,
            _messageDeliveryStatusMap,
        ) { msgs, statusMap ->
            msgs.map { message ->
                val updatedStatus = statusMap[message.id] ?: message.deliveryStatus
                message.copy(deliveryStatus = updatedStatus)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val messagesByDate: StateFlow<List<Pair<String, List<Message>>>> =
        messagesWithStatus
            .map { messages ->
                messages
                    .groupBy { message ->
                        message.timestamp.toMessengerFormattedDate()
                    }.toList()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val _selectedMedia = MutableStateFlow<Media?>(null)
    val selectedMedia = _selectedMedia.asStateFlow()

    init {
        setupMessageEventCollection()
        loadMessages()
        loadChat()
    }

    private fun loadChat() {
        viewModelScope.launch {
            _chat.value =
                chatRepository.getChatById(privateKeyViewModel.currentPrivateKey.value!!, chatId)
        }
    }

    private fun setupMessageEventCollection() {
        viewModelScope.launch {
            messagesRepository.messageEvents(chatId).collectLatest { event ->
                when (event) {
                    is MessageEvent.Added -> {
                        val currentMessages = _messages.value.toMutableList()
                        currentMessages.add(event.message)
                        _messages.value = currentMessages.sortedBy { it.timestamp }
                    }

                    is MessageEvent.UpdatedDeliveryStatus -> {
                        val currentMap = _messageDeliveryStatusMap.value.toMutableMap()
                        currentMap[event.messageId] = event.newStatus
                        _messageDeliveryStatusMap.value = currentMap
                    }
                }
            }
        }
    }

    fun loadMessages(
        offset: Int = 0,
        limit: Int = 50,
        refresh: Boolean = false,
    ) {
        viewModelScope.launch {
            if (!refresh) {
                _isLoading.value = true
            } else {
                _isRefreshing.value = true
            }
            privateKeyViewModel.currentPrivateKey.value?.let { privateKey ->
                try {
                    val loadedMessages =
                        messagesRepository.getMessages(
                            privateKey = privateKey,
                            chatId = chatId,
                            limit = limit,
                            offset = offset,
                            refresh = refresh,
                        )

                    _messageDeliveryStatusMap.value =
                        _messageDeliveryStatusMap.value.toMutableMap().apply {
                            putAll(loadedMessages.associate { it.id to it.deliveryStatus })
                        }

                    val byId = loadedMessages.associate { it.id to it }
                    val existingIds = hashSetOf<String>()

                    val messageList =
                        _messages.value.map { existing ->
                            existingIds.add(existing.id)
                            byId.getOrDefault(existing.id, existing)
                        }
                    val newMessages = loadedMessages.filter { !existingIds.contains(it.id) }
                    _messages.value = (messageList + newMessages).sortedBy { it.timestamp }
                } catch (e: Exception) {
                }
            }
            if (!refresh) {
                _isLoading.value = false
            } else {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshMessages(
        offset: Int = 0,
        limit: Int = 50,
    ) {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadMessages(offset = offset, limit = limit, refresh = true)
            messageUpdateService.fetchAndProcessUpdates()
            _isRefreshing.value = false
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            privateKeyViewModel.currentPrivateKey.value?.let { privateKey ->
                try {
                    val media = _selectedMedia.value
                    messagesRepository.sendMessage(
                        privateKey = privateKey,
                        chatId = chatId,
                        content = content,
                        mediaMime = media?.mimeType,
                        mediaBytes = media?.content,
                    )
                    _selectedMedia.value = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun sendMediaMessage(
        context: Context,
        uri: Uri,
        content: String = "",
    ) {
        viewModelScope.launch {
            privateKeyViewModel.currentPrivateKey.value?.let { privateKey ->
                withContext(Dispatchers.IO) {
                    val mimeType = FileUtil.getMimeType(context, uri) ?: "application/octet-stream"
                    val bytes = FileUtil.readBytes(context, uri)

                    if (bytes != null) {
                        messagesRepository.sendMessage(
                            privateKey = privateKey,
                            chatId = chatId,
                            content = content,
                            mediaMime = mimeType,
                            mediaBytes = bytes,
                        )
                    }
                }
            }
        }
    }

    fun resendMessage(messageId: String) {
        viewModelScope.launch {
            privateKeyViewModel.currentPrivateKey.value?.let { privateKey ->
                try {
                    messagesRepository.resendMessage(
                        privateKey = privateKey,
                        chatId = chatId,
                        messageId = messageId,
                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    fun createOpenMediaIntent(media: Media): Intent? {
        val ticket =
            contentProviderRepository.makeTicket(
                privateKeyViewModel.currentPrivateKey.value!!,
                media,
            )

        val contentUri = BlobContentProvider.getContentUri(ticket.id)

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, media.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun openMediaInExternalApp(
        context: Context,
        media: Media,
    ) {
        val intent = createOpenMediaIntent(media) ?: return
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast
                .makeText(
                    context,
                    "Не удалось найти приложения для этого типа файлов",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun sharePublicKey(context: Context) {
        viewModelScope.launch {
            val currentChat = chat.value!!
            viewModelScope.launch {
                val uri = BlobContentProvider.getPubKeyUri(currentChat.id)
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
    }
}
