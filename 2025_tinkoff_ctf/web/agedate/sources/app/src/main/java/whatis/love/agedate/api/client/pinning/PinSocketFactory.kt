package whatis.love.agedate.api.client.pinning

import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.Socket
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun OkHttpClient.Builder.agedatePinning(paramsRepo: PinningParametersRepo): OkHttpClient.Builder {
    val factory = PinSSLSocketFactory(paramsRepo)
    return this.sslSocketFactory(factory, factory.trustManager())
}

class PinSSLSocketFactory(
    paramsRepo: PinningParametersRepo,
) : SSLSocketFactory() {
    val trustManager: PinTrustManager = PinTrustManager(paramsRepo)
    private val sslSocketFactory: SSLSocketFactory = createSSLSocketFactory()

    private fun createSSLSocketFactory(): SSLSocketFactory {
        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            return sslContext.socketFactory
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to create SSL socket factory", e)
        } catch (e: KeyManagementException) {
            throw RuntimeException("Failed to create SSL socket factory", e)
        }
    }

    fun trustManager(): X509TrustManager = trustManager

    override fun createSocket(
        s: Socket?,
        host: String?,
        port: Int,
        autoClose: Boolean,
    ): Socket = sslSocketFactory.createSocket(s, host, port, autoClose)

    override fun createSocket(
        host: String?,
        port: Int,
    ): Socket = sslSocketFactory.createSocket(host, port)

    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int,
    ): Socket = sslSocketFactory.createSocket(host, port, localHost, localPort)

    override fun createSocket(
        host: InetAddress?,
        port: Int,
    ): Socket = sslSocketFactory.createSocket(host, port)

    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int,
    ): Socket = sslSocketFactory.createSocket(address, port, localAddress, localPort)

    override fun getDefaultCipherSuites(): Array<String> = sslSocketFactory.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = sslSocketFactory.supportedCipherSuites
}
