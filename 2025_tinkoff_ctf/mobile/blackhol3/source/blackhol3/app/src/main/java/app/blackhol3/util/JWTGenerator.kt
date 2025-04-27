package app.blackhol3.util

import android.util.Base64
import org.json.JSONObject
import java.security.Signature
import java.security.interfaces.RSAPrivateKey

object JWTGenerator {
    fun generateJWT(
        privateKey: RSAPrivateKey,
        keyId: String,
        issuer: String,
        subject: String,
        expirationTimeSeconds: Long,
        claims: Map<String, Any> = emptyMap(),
    ): String {
        val header =
            JSONObject().apply {
                put("alg", "RS256")
                put("typ", "JWT")
                put("kid", keyId)
            }

        val currentTimeMillis = System.currentTimeMillis()
        val payload =
            JSONObject().apply {
                put("iss", issuer)
                put("sub", subject)
                put("iat", currentTimeMillis / 1000)
                put("exp", currentTimeMillis / 1000 + expirationTimeSeconds)

                claims.forEach { (key, value) ->
                    put(key, value)
                }
            }

        val encodedHeader = encodeBase64Url(header.toString())
        val encodedPayload = encodeBase64Url(payload.toString())

        val contentToSign = "$encodedHeader.$encodedPayload"

        val signature = signWithRsa(contentToSign, privateKey)

        return "$contentToSign.$signature"
    }

    private fun signWithRsa(
        content: String,
        privateKey: RSAPrivateKey,
    ): String {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(content.toByteArray())

        val signatureBytes = signature.sign()
        return encodeBase64Url(signatureBytes)
    }

    private fun encodeBase64Url(data: String): String = encodeBase64Url(data.toByteArray())

    private fun encodeBase64Url(data: ByteArray): String =
        Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}
