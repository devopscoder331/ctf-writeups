package app.blackhol3.model

import android.graphics.Bitmap
import app.blackhol3.service.KeyPicGenerationService
import app.blackhol3.util.fingerprint
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec

@ConsistentCopyVisibility
data class PublicKey private constructor(
    val keyBytes: ByteArray,
) {
    lateinit var keyPic: Bitmap

    constructor(keyBytes: ByteArray, keyPic: Bitmap) : this(keyBytes) {
        this.keyPic = keyPic
    }

    constructor(keyBytes: ByteArray, keyPicGenerator: KeyPicGenerationService) : this(
        keyBytes,
    ) {
        keyPic = keyPicGenerator.visualizePublicKey(rsaPublicKey)
    }

    companion object {
        private val rsaKeyFactory =
            KeyFactory.getInstance("RSA")
    }

    val rsaPublicKey by lazy {
        rsaKeyFactory.generatePublic(
            X509EncodedKeySpec(keyBytes),
        ) as RSAPublicKey
    }

    @OptIn(ExperimentalStdlibApi::class)
    val fingerprint by lazy {
        rsaPublicKey.fingerprint()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicKey

        if (!keyBytes.contentEquals(other.keyBytes)) return false
        if (keyPic != other.keyPic) return false
        if (rsaPublicKey != other.rsaPublicKey) return false
        if (fingerprint != other.fingerprint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyBytes.contentHashCode()
        result = 31 * result + keyPic.hashCode()
        result = 31 * result + rsaPublicKey.hashCode()
        result = 31 * result + fingerprint.hashCode()
        return result
    }
}
