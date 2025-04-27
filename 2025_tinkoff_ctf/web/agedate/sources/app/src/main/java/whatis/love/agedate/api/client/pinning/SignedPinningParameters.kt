package whatis.love.agedate.api.client.pinning

import kotlinx.serialization.Serializable

@Serializable
data class SignedPinningParameters(
    val apiRoot: String,
    val fingerprint: String,
    @Serializable(with = ByteArrayAsBase64Serializer::class)
    val signature: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignedPinningParameters

        if (apiRoot != other.apiRoot) return false
        if (fingerprint != other.fingerprint) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = apiRoot.hashCode()
        result = 31 * result + fingerprint.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }

    companion object {
        fun bytesToSign(
            root: String,
            fingerprint: String,
        ): ByteArray =
            root.toByteArray(Charsets.UTF_8) + ":::".toByteArray() +
                fingerprint.toByteArray(
                    Charsets.UTF_8,
                )
    }

    fun bytesToSign() = bytesToSign(apiRoot, fingerprint)
}
