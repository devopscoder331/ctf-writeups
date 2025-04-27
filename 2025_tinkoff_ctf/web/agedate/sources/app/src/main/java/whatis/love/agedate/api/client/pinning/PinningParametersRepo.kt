package whatis.love.agedate.api.client.pinning

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.signers.RSADigestSigner
import java.io.InputStream

class InvalidPinningParametersSignatureException : Exception()

class PinningParametersRepo(
    private val persistence: PinningParametersPersistence,
) {
    private val pinningParametersFetcher = OkHttpClient.Builder().build()

    @OptIn(ExperimentalSerializationApi::class)
    fun unwrapPinningParameters(response: InputStream): PinningParameters {
        val params = Json.decodeFromStream<SignedPinningParameters>(response)

        val bytes = params.bytesToSign()

        RSADigestSigner(SHA512Digest()).apply {
            init(false, PinningParametersPublicKey)
            update(bytes, 0, bytes.size)
            if (!verifySignature(params.signature)) {
                throw InvalidPinningParametersSignatureException()
            }
        }

        return params.let { PinningParameters(it.apiRoot, it.fingerprint) }
    }

    private fun fetchPinningParameters(): PinningParameters {
        val req =
            Request
                .Builder()
                .url(PinningParametersEndpoint)
                .get()
                .build()
        val response = pinningParametersFetcher.newCall(req).execute()
        if (!response.isSuccessful) throw Exception("Unexpected code $response")
        val newParams = response.body!!.byteStream().use { unwrapPinningParameters(it) }
        persistence.setPinningParameters(newParams)
        return newParams
    }

    fun getHost(forceUpdate: Boolean = false): String {
        val current = persistence.getPinningParameters()
        if (current.apiRoot == null || forceUpdate) {
            return fetchPinningParameters().apiRoot!!
        }
        return current.apiRoot
    }

    fun getFingerprint(forceUpdate: Boolean = false): String {
        val current = persistence.getPinningParameters()
        if (current.fingerprint == null || forceUpdate) {
            return fetchPinningParameters().fingerprint!!
        }
        return current.fingerprint
    }
}
