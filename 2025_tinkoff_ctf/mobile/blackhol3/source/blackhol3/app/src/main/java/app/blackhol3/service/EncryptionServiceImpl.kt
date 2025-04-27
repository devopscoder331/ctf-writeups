package app.blackhol3.service

import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionServiceImpl : EncryptionService {
    companion object {
        private const val RSA_CIPHER_TRANSFORMATION = "RSA/NONE/OAEPWITHSHA-256ANDMGF1PADDING"
        private const val AES_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }

    override fun encryptBytes(
        key: PrivateKey,
        data: ByteArray,
    ): ByteArray = encrypt(key.rsaPublicKey, data)

    fun encryptBytes(
        key: PublicKey,
        data: ByteArray,
    ): ByteArray = encrypt(key.rsaPublicKey, data)

    override fun decryptBytes(
        key: PrivateKey,
        encrypted: ByteArray,
    ): ByteArray = decrypt(key.rsaPrivateKey, encrypted)

    override fun encryptString(
        key: PrivateKey,
        plaintext: String,
    ): ByteArray = encryptBytes(key, plaintext.toByteArray(Charsets.UTF_8))

    override fun encryptString(
        key: PublicKey,
        plaintext: String,
    ): ByteArray = encryptBytes(key, plaintext.toByteArray(Charsets.UTF_8))

    override fun decryptString(
        key: PrivateKey,
        encrypted: ByteArray,
    ): String = decryptBytes(key, encrypted).toString(Charsets.UTF_8)

    private fun encrypt(
        rsaPublicKey: RSAPublicKey,
        data: ByteArray,
    ): ByteArray {
        val aesKey =
            KeyGenerator
                .getInstance("AES")
                .apply {
                    init(AES_KEY_SIZE)
                }.generateKey()

        val iv =
            ByteArray(GCM_IV_LENGTH).apply {
                SecureRandom().nextBytes(this)
            }

        val encryptedData =
            Cipher
                .getInstance(AES_CIPHER_TRANSFORMATION)
                .apply {
                    init(ENCRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
                }.doFinal(data)

        val encryptedKey =
            Cipher
                .getInstance(RSA_CIPHER_TRANSFORMATION)
                .apply {
                    init(ENCRYPT_MODE, rsaPublicKey)
                }.doFinal(aesKey.encoded)

        val encryptedKeyLength = encryptedKey.size
        return ByteBuffer
            .allocate(4 + encryptedKeyLength + iv.size + encryptedData.size)
            .apply {
                putInt(encryptedKeyLength)
                put(encryptedKey)
                put(iv)
                put(encryptedData)
            }.array()
    }

    private fun decrypt(
        rsaPrivateKey: RSAPrivateKey,
        encrypted: ByteArray,
    ): ByteArray {
        val buffer = ByteBuffer.wrap(encrypted)
        val encryptedKeyLength = buffer.int

        val encryptedKeyBytes = ByteArray(encryptedKeyLength).also { buffer.get(it) }
        val decryptedKeyBytes =
            Cipher
                .getInstance(RSA_CIPHER_TRANSFORMATION)
                .apply {
                    init(Cipher.DECRYPT_MODE, rsaPrivateKey)
                }.doFinal(encryptedKeyBytes)
        val aesKey = SecretKeySpec(decryptedKeyBytes, "AES")

        val iv = ByteArray(GCM_IV_LENGTH).also { buffer.get(it) }
        val encryptedData = ByteArray(buffer.remaining()).also { buffer.get(it) }

        return Cipher
            .getInstance(AES_CIPHER_TRANSFORMATION)
            .apply {
                init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
            }.doFinal(encryptedData)
    }
}
