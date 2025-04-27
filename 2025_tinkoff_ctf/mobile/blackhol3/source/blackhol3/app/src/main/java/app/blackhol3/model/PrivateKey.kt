package app.blackhol3.model

import android.graphics.Bitmap
import app.blackhol3.service.KeyPicGenerationService
import app.blackhol3.util.fingerprint
import java.security.KeyFactory
import java.security.KeyPair
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.RSAPublicKeySpec

@ConsistentCopyVisibility
data class PrivateKey private constructor(
    val id: String,
    val privateKeyBytes: ByteArray,
) {
    lateinit var keyPic: Bitmap

    constructor(id: String, keyBytes: ByteArray, keyPic: Bitmap) : this(
        id,
        keyBytes,
    ) {
        this.keyPic = keyPic
    }

    constructor(
        id: String,
        keyBytes: ByteArray,
        keyPicGenerator: KeyPicGenerationService,
    ) : this(
        id,
        keyBytes,
    ) {
        keyPic = keyPicGenerator.visualizePublicKey(rsaPublicKey)
    }

    companion object {
        private val rsaKeyFactory =
            KeyFactory.getInstance("RSA")
        private val publicExponent = RSAKeyGenParameterSpec.F4
    }

    val rsaPrivateKey by lazy {
        rsaKeyFactory.generatePrivate(
            PKCS8EncodedKeySpec(privateKeyBytes),
        ) as RSAPrivateKey
    }

    val rsaPublicKey by lazy {
        rsaKeyFactory.generatePublic(
            RSAPublicKeySpec(
                rsaPrivateKey.modulus,
                publicExponent,
            ),
        ) as RSAPublicKey
    }

    val keyPair by lazy {
        KeyPair(rsaPublicKey, rsaPrivateKey)
    }

    val fingerprint by lazy {
        rsaPublicKey.fingerprint()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateKey

        if (id != other.id) return false
        if (!privateKeyBytes.contentEquals(other.privateKeyBytes)) return false
        if (keyPic != other.keyPic) return false
        if (rsaPrivateKey != other.rsaPrivateKey) return false
        if (rsaPublicKey != other.rsaPublicKey) return false
        if (keyPair != other.keyPair) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + privateKeyBytes.contentHashCode()
        result = 31 * result + keyPic.hashCode()
        result = 31 * result + rsaPrivateKey.hashCode()
        result = 31 * result + rsaPublicKey.hashCode()
        result = 31 * result + keyPair.hashCode()
        return result
    }
}
