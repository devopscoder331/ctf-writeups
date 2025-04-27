package app.blackhol3.util

import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun RSAPublicKey.toPEM(): String =
    """-----BEGIN PUBLIC KEY-----
    |${Base64.encode(this.encoded).chunked(64).joinToString("\n")}
    |-----END PUBLIC KEY-----
    |
    """.trimMargin()

@OptIn(ExperimentalStdlibApi::class)
fun RSAPublicKey.fingerprint(): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(this.encoded)
    return md.digest().toHexString()
}
