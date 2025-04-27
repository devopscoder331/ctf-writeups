package app.blackhol3.data.local.dao

import android.database.Cursor
import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.data.local.contract.MessageContract
import app.blackhol3.data.local.envelope.LocalMessageEnvelope
import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Message
import app.blackhol3.model.PrivateKey
import app.blackhol3.service.EncryptionService

class LocalMessageDaoImpl(
    val db: DatabaseHelper,
    val encryptionService: EncryptionService,
) : LocalMessageDao {
    override fun saveMessage(
        privateKey: PrivateKey,
        message: Message,
        status: DeliveryStatus,
    ): Long {
        val envelope = LocalMessageEnvelope.fromMessage(message)
        val encrypted = envelope.encrypt(encryptionService, privateKey)
        return MessageContract.saveMessageEnvelope(
            db,
            message.id,
            message.chatId,
            encrypted,
            status,
        )
    }

    override fun updateMessageStatus(
        privateKey: PrivateKey,
        messageId: String,
        status: DeliveryStatus,
        timestamp: Long,
    ) = MessageContract.updateMessageStatus(
        db,
        messageId,
        status,
    )

    override fun getMessages(
        privateKey: PrivateKey,
        chatId: String,
        offset: Int,
        limit: Int,
    ): List<Message> =
        MessageContract.loadMessages(db, chatId, offset, limit) { cursor ->
            messageFromCursor(privateKey, cursor)
        }

    override fun getMessagesWithTimestamps(
        privateKey: PrivateKey,
        chatId: String,
        timestamps: Set<Long>,
    ): List<Message> {
        val messages =
            MessageContract.loadAll(db, chatId) { cursor ->
                messageFromCursor(privateKey, cursor)
            }
        return messages.filter { it.timestamp in timestamps }
    }

    private fun messageFromCursor(
        privateKey: PrivateKey,
        cursor: Cursor,
    ): Message? {
        val decrypted: LocalMessageEnvelope
        try {
            decrypted =
                LocalMessageEnvelope.decrypt(
                    encryptionService,
                    privateKey,
                    cursor.getBlob(cursor.getColumnIndexOrThrow(MessageContract.COLUMN_CONTENT)),
                )
        } catch (e: Exception) {
            return null
        }
        return Message(
            id = cursor.getString(cursor.getColumnIndexOrThrow(MessageContract.COLUMN_ID)),
            chatId = cursor.getString(cursor.getColumnIndexOrThrow(MessageContract.COLUMN_CHAT_ID)),
            seq = cursor.getLong(cursor.getColumnIndexOrThrow(MessageContract.COLUMN_CHAT_SEQ)),
            deliveryStatus =
                DeliveryStatus.valueOf(
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            MessageContract.COLUMN_DELIVERY_STATUS,
                        ),
                    ),
                ),
            content = decrypted.content,
            timestamp = decrypted.timestamp,
            mediaRef = decrypted.mediaRef,
        )
    }

    override fun getById(
        privateKey: PrivateKey,
        messageId: String,
    ): Message? =
        MessageContract.getById(db, messageId) { cursor ->
            messageFromCursor(
                privateKey,
                cursor,
            )
        }
}
