package app.blackhol3.repository

import app.blackhol3.model.Message
import app.blackhol3.model.MessageEvent
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {
    suspend fun getMessages(
        privateKey: PrivateKey,
        chatId: String,
        limit: Int = 50,
        offset: Int = 0,
        refresh: Boolean = false,
    ): List<Message>

    fun messageEvents(chatId: String): Flow<MessageEvent>

    suspend fun lastMessageFeed(
        privateKey: PrivateKey,
        chatId: String,
    ): Flow<Message?>

    suspend fun sendMessage(
        privateKey: PrivateKey,
        chatId: String,
        content: String,
        mediaMime: String? = null,
        mediaBytes: ByteArray? = null,
    ): Message

    suspend fun resendMessage(
        privateKey: PrivateKey,
        chatId: String,
        messageId: String,
    )

    suspend fun incomingMessage(
        privateKey: PrivateKey,
        senderPublicKey: PublicKey,
        message: Message,
    ): Message
}
