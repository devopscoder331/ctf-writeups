package whatis.love.agedate.api.client.pinning

import java.net.Socket
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509ExtendedTrustManager

@Suppress("CustomX509TrustManager")
class PinTrustManager(
    val pinningParamsRepo: PinningParametersRepo,
) : X509ExtendedTrustManager() {
    private val systemTrustManager: X509ExtendedTrustManager

    init {
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers
        systemTrustManager =
            trustManagers.first { it is X509ExtendedTrustManager } as X509ExtendedTrustManager
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = systemTrustManager.acceptedIssuers

    private fun validateLeaf(chain: Array<out X509Certificate?>?) {
        if (chain == null || chain.isEmpty()) {
            throw CertificateException("Certificate chain is empty")
        }

        val leafCert = chain.first()
        if (leafCert == null) {
            throw CertificateException("Leaf certificate is null")
        }

        val certFingerprint = calculateSHA256Fingerprint(leafCert)

        val probablyCachedFingerprint = pinningParamsRepo.getFingerprint()
        if (certFingerprint != probablyCachedFingerprint) {
            val freshFingerprint = pinningParamsRepo.getFingerprint(true)
            if (certFingerprint != freshFingerprint) {
                throw CertificateException("Certificate pinning validation failed")
            }
        }
    }

    private fun calculateSHA256Fingerprint(cert: X509Certificate): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(cert.encoded)
        return digest.joinToString("") { "%02x".format(it) }
    }

    override fun checkServerTrusted(
        chain: Array<out X509Certificate>,
        authType: String,
    ) {
        systemTrustManager.checkServerTrusted(chain, authType)
        validateLeaf(chain)
    }

    override fun checkServerTrusted(
        chain: Array<out X509Certificate?>?,
        authType: String?,
        socket: Socket?,
    ) {
        systemTrustManager.checkServerTrusted(chain, authType, socket)
        chain?.let { validateLeaf(it) }
    }

    override fun checkServerTrusted(
        chain: Array<out X509Certificate?>?,
        authType: String?,
        engine: SSLEngine?,
    ) {
        systemTrustManager.checkServerTrusted(chain, authType, engine)
        chain?.let { validateLeaf(it) }
    }

    override fun checkClientTrusted(
        chain: Array<out X509Certificate?>?,
        authType: String?,
        socket: Socket?,
    ): Unit = throw CertificateException("Client certificates are not supported by PinTrustManager")

    override fun checkClientTrusted(
        chain: Array<out X509Certificate>,
        authType: String,
    ): Unit = throw CertificateException("Client certificates are not supported by PinTrustManager")

    override fun checkClientTrusted(
        chain: Array<out X509Certificate?>?,
        authType: String?,
        engine: SSLEngine?,
    ): Unit = throw CertificateException("Client certificates are not supported by PinTrustManager")
}
