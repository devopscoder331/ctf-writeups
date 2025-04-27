package app.blackhol3.data.local.dao

import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Message
import app.blackhol3.model.PrivateKey

interface LocalMessageDao {
    fun saveMessage(
        privateKey: PrivateKey,
        message: Message,
        status: DeliveryStatus,
    ): Long

    fun updateMessageStatus(
        privateKey: PrivateKey,
        messageId: String,
        status: DeliveryStatus,
        timestamp: Long,
    )

    fun getMessages(
        privateKey: PrivateKey,
        chatId: String,
        offset: Int,
        limit: Int,
    ): List<Message>

    fun getMessagesWithTimestamps(
        privateKey: PrivateKey,
        chatId: String,
        timestamps: Set<Long>,
    ): List<Message>

    fun getById(
        privateKey: PrivateKey,
        messageId: String,
    ): Message?
}
