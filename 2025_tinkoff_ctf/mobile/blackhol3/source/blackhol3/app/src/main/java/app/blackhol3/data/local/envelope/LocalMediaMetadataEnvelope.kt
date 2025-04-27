package app.blackhol3.data.local.envelope

import app.blackhol3.model.PrivateKey
import app.blackhol3.service.EncryptionService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LocalMediaMetadataEnvelope(
    val mime: String,
    val size: Long,
) {
    fun encrypt(
        service: EncryptionService,
        privateKey: PrivateKey,
    ): ByteArray {
        val serialized = Json.encodeToString(serializer(), this)
        return service.encryptString(privateKey, serialized)
    }

    companion object {
        fun decrypt(
            service: EncryptionService,
            privateKey: PrivateKey,
            envelopeBytes: ByteArray,
        ): LocalMediaMetadataEnvelope {
            val decrypted = service.decryptString(privateKey, envelopeBytes)
            val envelope = Json.decodeFromString(serializer(), decrypted)
            return envelope
        }
    }
}
