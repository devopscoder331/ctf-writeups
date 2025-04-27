package whatis.love.agedate.api.client.pinning

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.params.AsymmetricKeyParameter
import org.bouncycastle.crypto.util.PublicKeyFactory
import org.bouncycastle.openssl.PEMParser
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun publicKeyFromPem(pemString: String): AsymmetricKeyParameter =
    PEMParser(pemString.reader()).use { parser ->
        PublicKeyFactory.createKey(parser.readObject() as SubjectPublicKeyInfo)
    }

@OptIn(ExperimentalEncodingApi::class)
object ByteArrayAsBase64Serializer : KSerializer<ByteArray> {
    private val base64 = Base64.Default

    override val descriptor: SerialDescriptor
        get() =
            PrimitiveSerialDescriptor(
                "ByteArrayAsBase64Serializer",
                PrimitiveKind.STRING,
            )

    override fun serialize(
        encoder: Encoder,
        value: ByteArray,
    ) {
        val base64Encoded = base64.encode(value)
        encoder.encodeString(base64Encoded)
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        val base64Decoded = decoder.decodeString()
        return base64.decode(base64Decoded)
    }
}
