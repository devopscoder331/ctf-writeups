package app.blackhol3.service

import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey

interface EncryptionService {
    fun encryptBytes(
        key: PrivateKey,
        data: ByteArray,
    ): ByteArray

    fun decryptBytes(
        key: PrivateKey,
        data: ByteArray,
    ): ByteArray

    fun encryptString(
        key: PrivateKey,
        data: String,
    ): ByteArray

    fun encryptString(
        key: PublicKey,
        data: String,
    ): ByteArray

    fun decryptString(
        key: PrivateKey,
        data: ByteArray,
    ): String
}
