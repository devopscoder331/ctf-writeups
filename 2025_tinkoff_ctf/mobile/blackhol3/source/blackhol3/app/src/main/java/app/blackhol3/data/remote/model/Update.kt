package app.blackhol3.data.remote.model

import app.blackhol3.data.remote.envelope.RemoteMessageEnvelope
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import app.blackhol3.service.EncryptionService
import app.blackhol3.service.KeyPicGenerationService
import app.blackhol3.util.ByteArrayAsBase64Serializer
import kotlinx.serialization.Serializable

@Serializable
data class Update(
    @Serializable(with = ByteArrayAsBase64Serializer::class)
    val pubkeyBytes: ByteArray,
    @Serializable(with = ByteArrayAsBase64Serializer::class)
    val envelopeBytes: ByteArray,
) {
    fun pubKey(keyPicGenerationService: KeyPicGenerationService): PublicKey = PublicKey(pubkeyBytes, keyPicGenerationService)

    fun envelope(
        encryptionService: EncryptionService,
        privateKey: PrivateKey,
    ): RemoteMessageEnvelope =
        RemoteMessageEnvelope.decrypt(
            encryptionService,
            privateKey,
            envelopeBytes,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Update

        if (!pubkeyBytes.contentEquals(other.pubkeyBytes)) return false
        if (!envelopeBytes.contentEquals(other.envelopeBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pubkeyBytes.contentHashCode()
        result = 31 * result + envelopeBytes.contentHashCode()
        return result
    }
}
