package app.blackhol3.data.remote.envelope

import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Media
import app.blackhol3.model.Message
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import app.blackhol3.service.EncryptionService
import app.blackhol3.util.ByteArrayAsBase64Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class RemoteMessageEnvelope(
    val envelopeVersionId: Int = 1,
    val content: String,
    val generatedTimestamp: Long,
    val mediaMime: String? = null,
    val mediaSize: Long? = null,
    @Serializable(with = ByteArrayAsBase64Serializer::class)
    val mediaBytes: ByteArray? = null,
) {
    companion object {
        fun decrypt(
            encryptionService: EncryptionService,
            privateKey: PrivateKey,
            data: ByteArray,
        ): RemoteMessageEnvelope {
            val decryptedMessage = encryptionService.decryptString(privateKey, data)
            val envelope =
                Json.decodeFromString(serializer(), decryptedMessage)
            return envelope
        }
    }

    fun encrypt(
        encryptionService: EncryptionService,
        publicKey: PublicKey,
    ): ByteArray {
        val serialized = Json.encodeToString(serializer(), this)
        return encryptionService.encryptString(publicKey, serialized)
    }

    fun toMessage(
        id: String = UUID.randomUUID().toString(),
        chatId: String,
        deliveryStatus: DeliveryStatus = DeliveryStatus.INCOMING,
    ): Message {
        val mediaRef =
            if (mediaBytes != null) {
                UUID.randomUUID().toString()
            } else {
                null
            }

        val media =
            mediaRef?.let {
                Media(
                    id = it,
                    mimeType = mediaMime!!,
                    sizeBytes = mediaSize!!,
                    content = mediaBytes,
                )
            }

        return Message(
            id = id,
            chatId = chatId,
            seq = -1,
            deliveryStatus = deliveryStatus,
            content = this.content,
            timestamp = this.generatedTimestamp,
            media = media,
            mediaRef = mediaRef,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteMessageEnvelope

        if (envelopeVersionId != other.envelopeVersionId) return false
        if (generatedTimestamp != other.generatedTimestamp) return false
        if (mediaSize != other.mediaSize) return false
        if (content != other.content) return false
        if (mediaMime != other.mediaMime) return false
        if (!mediaBytes.contentEquals(other.mediaBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = envelopeVersionId
        result = 31 * result + generatedTimestamp.hashCode()
        result = 31 * result + (mediaSize?.hashCode() ?: 0)
        result = 31 * result + content.hashCode()
        result = 31 * result + (mediaMime?.hashCode() ?: 0)
        result = 31 * result + (mediaBytes?.contentHashCode() ?: 0)
        return result
    }
}

fun Message.toRemoteMessageEnvelope(): RemoteMessageEnvelope {
    if (this.media != null && this.media.content == null) {
        throw IllegalArgumentException("Media content cannot be null. Please hydrate it first.")
    }
    return RemoteMessageEnvelope(
        content = this.content,
        generatedTimestamp = this.timestamp,
        mediaMime = this.media?.mimeType,
        mediaSize = this.media?.sizeBytes,
        mediaBytes = this.media?.content,
    )
}
