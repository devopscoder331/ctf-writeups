package app.blackhol3.repository

import app.blackhol3.data.local.dao.LocalMessageDao
import app.blackhol3.data.local.dao.MediaDao
import app.blackhol3.data.local.model.Chat
import app.blackhol3.data.remote.dao.RemoteMessageDao
import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Media
import app.blackhol3.model.Message
import app.blackhol3.model.MessageEvent
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.max

class MessagesRepositoryImpl(
    val localMessageDao: LocalMessageDao,
    val remoteMessageDao: RemoteMessageDao,
    val chatRepository: ChatRepository,
    val mediaDao: MediaDao,
) : MessagesRepository {
    private val messageUpdates =
        MutableSharedFlow<Pair<String, MessageEvent>>(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    private val lastMessagesMap = mutableMapOf<String, MutableStateFlow<Message?>>()

    override suspend fun getMessages(
        privateKey: PrivateKey,
        chatId: String,
        limit: Int,
        offset: Int,
        refresh: Boolean,
    ): List<Message> =
        withContext(Dispatchers.IO) {
            val chat =
                chatRepository.getChatById(privateKey, chatId) ?: return@withContext emptyList()
            val limitOffsetMsg =
                localMessageDao.getMessages(privateKey, chatId, 1, max(offset - 1, 0)).firstOrNull()
            if (refresh) {
                mergeRemoteMessages(
                    privateKey,
                    chat,
                    limit,
                    limitOffsetMsg?.timestamp ?: System.currentTimeMillis(),
                )
            }

            val localMessages = localMessageDao.getMessages(privateKey, chatId, limit, offset)
            if (localMessages.isEmpty() && !refresh) {
                mergeRemoteMessages(privateKey, chat, limit, limitOffsetMsg?.timestamp ?: 0)
            }

            val localMessages2 = localMessageDao.getMessages(privateKey, chatId, limit, offset)

            val mediaHydrated =
                localMessages2.map { msg ->
                    msg.mediaRef?.let {
                        val media = mediaDao.getMedia(privateKey, it)
                        media?.let { msg.copy(media = media) } ?: msg
                    } ?: msg
                }
            mediaHydrated
        }

    private suspend fun mergeRemoteMessages(
        privateKey: PrivateKey,
        chat: Chat,
        limit: Int,
        since: Long,
    ): List<Message> =
        withContext(Dispatchers.IO) {
            val messages =
                try {
                    remoteMessageDao.getMessages(
                        privateKey,
                        chat,
                        limit,
                        since,
                    )
                } catch (e: Exception) {
                    emptyList<Message>()
                }
            val byId = messages.associateBy { it.id }.toMutableMap()
            val byTimestamp = messages.groupBy { it.timestamp }
            val existing =
                localMessageDao.getMessagesWithTimestamps(
                    privateKey,
                    chat.id,
                    byTimestamp.keys,
                )

            existing.forEach { e ->
                byTimestamp[e.timestamp]
                    ?.findLast { it.deliveryStatus != DeliveryStatus.INCOMING && it.deliveryStatus != e.deliveryStatus }
                    ?.let {
                        localMessageDao.updateMessageStatus(
                            privateKey,
                            e.id,
                            it.deliveryStatus,
                            it.timestamp,
                        )
                    }
                byTimestamp[e.timestamp]?.forEach {
                    byId.remove(it.id)
                }
            }

            byId.forEach {
                incomingMessage(privateKey, chat.pubKey, it.value)
            }

            byId.values.toList()
        }

    override fun messageEvents(chatId: String): Flow<MessageEvent> = messageUpdates.filter { it.first == chatId }.map { it.second }

    override suspend fun lastMessageFeed(
        privateKey: PrivateKey,
        chatId: String,
    ): Flow<Message?> =
        lastMessagesMap.getOrPut(chatId) {
            MutableStateFlow(
                getMessages(privateKey, chatId, limit = 1, offset = 0).firstOrNull(),
            )
        }

    override suspend fun sendMessage(
        privateKey: PrivateKey,
        chatId: String,
        content: String,
        mediaMime: String?,
        mediaBytes: ByteArray?,
    ): Message =
        withContext(Dispatchers.IO) {
            val media =
                mediaMime?.let {
                    val media =
                        Media(
                            id = UUID.randomUUID().toString(),
                            mimeType = mediaMime,
                            content = mediaBytes!!,
                            sizeBytes = mediaBytes.size.toLong(),
                        )
                    mediaDao.saveMedia(
                        privateKey,
                        media,
                    )
                    media
                }

            val message =
                Message(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    deliveryStatus = DeliveryStatus.SENT,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    media = media,
                )

            val chat =
                chatRepository.getChatById(privateKey, chatId) ?: throw Exception("Chat not found")

            val localSeq =
                localMessageDao.saveMessage(
                    privateKey,
                    message,
                    DeliveryStatus.SENT,
                )
            pokeChat(privateKey, chatId, message)

            sendMessageToServer(
                privateKey,
                chat,
                message.id,
            )

            message.copy(
                seq = localSeq,
            )
        }

    private suspend fun pokeChat(
        privateKey: PrivateKey,
        chatId: String,
        message: Message,
    ) {
        messageUpdates.emit(chatId to MessageEvent.Added(message))
        lastMessagesMap.getOrDefault(chatId, null)?.let {
            it.value = message
        }
        chatRepository.refreshChats(privateKey)
    }

    private suspend fun sendMessageToServer(
        privateKey: PrivateKey,
        chat: Chat,
        messageId: String,
    ) = withContext(Dispatchers.IO) {
        val message = localMessageDao.getById(privateKey, messageId) ?: return@withContext
        val media =
            message.mediaRef?.let {
                mediaDao.getMedia(privateKey, message.mediaRef) ?: return@withContext
            }
        updateMessageStatus(
            privateKey,
            chat.id,
            messageId,
            remoteMessageDao.sendMessage(
                privateKey,
                chat,
                message.copy(media = media),
            ),
            message.timestamp,
        )
    }

    override suspend fun resendMessage(
        privateKey: PrivateKey,
        chatId: String,
        messageId: String,
    ) {
        val chat =
            chatRepository.getChatById(privateKey, chatId) ?: throw Exception("Chat not found")
        sendMessageToServer(privateKey, chat, messageId)
    }

    override suspend fun incomingMessage(
        privateKey: PrivateKey,
        senderPublicKey: PublicKey,
        message: Message,
    ): Message {
        val chat = chatRepository.ensureChatByPubKey(privateKey, senderPublicKey)

        message.media?.let {
            mediaDao.saveMedia(
                privateKey,
                it,
            )
            it
        }

        val localSeq =
            localMessageDao.saveMessage(
                privateKey,
                message.copy(chatId = chat.id),
                DeliveryStatus.INCOMING,
            )
        pokeChat(privateKey, chat.id, message)
        return message.copy(
            seq = localSeq,
        )
    }

    private suspend fun updateMessageStatus(
        privateKey: PrivateKey,
        chatId: String,
        messageId: String,
        status: DeliveryStatus,
        timestamp: Long,
    ) {
        withContext(Dispatchers.IO) {
            localMessageDao.updateMessageStatus(privateKey, messageId, status, timestamp)
            messageUpdates.tryEmit(chatId to MessageEvent.UpdatedDeliveryStatus(messageId, status))
            chatRepository.refreshChats(privateKey)
        }
    }
}
