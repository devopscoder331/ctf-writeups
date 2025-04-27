package app.blackhol3.data.remote

import android.util.Log
import app.blackhol3.data.remote.model.Update
import app.blackhol3.model.PrivateKey
import app.blackhol3.util.JWTGenerator
import app.blackhol3.util.fingerprint
import app.blackhol3.util.toPEM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.min
import kotlin.math.pow

class MessageWebSocketClient(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val issuer: String,
    private val subject: String,
) {
    companion object {
        private const val JWT_EXPIRATION_SECONDS = 300L
        private const val INITIAL_RECONNECT_DELAY_MS = 5000L
        private const val MAX_RECONNECT_DELAY_MS = 30000L
        private const val RECONNECT_BACKOFF_MULTIPLIER = 1.5f
    }

    private var webSocket: WebSocket? = null
    private val listeners = mutableListOf<UpdateListener>()
    private var reconnectAttempts = 0
    private var reconnectJob: Job? = null
    private var registerJob: Job? = null
    private var currentPrivateKey: PrivateKey? = null
    private var isConnecting = false
    private var isIntentionalDisconnect = false

    interface UpdateListener {
        fun onUpdate(update: Update)

        fun onError(error: Throwable)

        fun onConnectionStateChange(connected: Boolean)
    }

    suspend fun connect(privateKey: PrivateKey) {
        currentPrivateKey = privateKey
        isIntentionalDisconnect = false

        if (isConnecting) return
        isConnecting = true

        try {
            val token =
                JWTGenerator.generateJWT(
                    privateKey = privateKey.rsaPrivateKey,
                    keyId = privateKey.rsaPublicKey.fingerprint(),
                    issuer = issuer,
                    subject = subject,
                    expirationTimeSeconds = JWT_EXPIRATION_SECONDS,
                )

            val wsUrl =
                "$baseUrl/ws/updates"
                    .toHttpUrl()
                    .newBuilder()
                    .build()
                    .toString()

            val request =
                Request
                    .Builder()
                    .url(wsUrl)
                    .header("Authorization", "Bearer $token")
                    .build()

            webSocket = client.newWebSocket(request, createWebSocketListener())

            reconnectAttempts = 0
        } catch (e: Exception) {
            Log.e("MessageWebSocketClient", "Connection failed", e)
            notifyErrorToListeners(e)
            scheduleReconnect()
        } finally {
            isConnecting = false
        }
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(
                webSocket: WebSocket,
                response: Response,
            ) {
                reconnectJob?.cancel()
                reconnectJob = null
                reconnectAttempts = 0

                Log.d("MessageWebSocketClient", "WebSocket connection established")
                listeners.forEach { it.onConnectionStateChange(true) }
            }

            @OptIn(ExperimentalEncodingApi::class)
            override fun onMessage(
                webSocket: WebSocket,
                text: String,
            ) {
                try {
                    val updateBytes = Base64.decode(text)
                    val parsed = Json.decodeFromString<Update>(updateBytes.decodeToString())

                    listeners.forEach { it.onUpdate(parsed) }
                } catch (e: Exception) {
                    Log.e("MessageWebSocketClient", "Failed to process message", e)
                    notifyErrorToListeners(e)
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?,
            ) {
                Log.e("MessageWebSocketClient", "WebSocket failure: ${t.message}", t)

                notifyErrorToListeners(t)
                listeners.forEach { it.onConnectionStateChange(false) }

                this@MessageWebSocketClient.webSocket = null

                if (!isIntentionalDisconnect) {
                    if (response?.code == 401) {
                        registerJob?.cancel()
                        registerJob =
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(calculateReconnectDelay())
                                try {
                                    val privateKey = currentPrivateKey ?: return@launch
                                    val registered = registerPublicKey(privateKey)
                                    if (registered) {
                                        connect(privateKey)
                                    } else {
                                        scheduleReconnect()
                                    }
                                } catch (e: Exception) {
                                    Log.e("MessageWebSocketClient", "Failed to register after 401", e)
                                    scheduleReconnect()
                                }
                            }
                    } else {
                        scheduleReconnect()
                    }
                }
            }

            override fun onClosing(
                webSocket: WebSocket,
                code: Int,
                reason: String,
            ) {
                webSocket.close(code, "OK, BYE!")
            }

            override fun onClosed(
                webSocket: WebSocket,
                code: Int,
                reason: String,
            ) {
                Log.d("MessageWebSocketClient", "WebSocket closed: $reason (code: $code)")
                if (code == 1008) {
                    registerJob?.cancel()
                    registerJob =
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(calculateReconnectDelay())
                                val privateKey = currentPrivateKey ?: return@launch
                                val registered = registerPublicKey(privateKey)
                                if (registered) {
                                    connect(privateKey)
                                } else {
                                    scheduleReconnect()
                                }
                            } catch (e: Exception) {
                                Log.e("MessageWebSocketClient", "Failed to register after 1008", e)
                                scheduleReconnect()
                            }
                        }
                }
                listeners.forEach { it.onConnectionStateChange(false) }
                this@MessageWebSocketClient.webSocket = null

                if (!isIntentionalDisconnect) {
                    scheduleReconnect()
                }
            }
        }
    }

    private fun scheduleReconnect() {
        if (isIntentionalDisconnect) return
        reconnectJob?.cancel()
        val delay = calculateReconnectDelay()

        Log.d(
            "MessageWebSocketClient",
            "Scheduling reconnect in ${delay}ms (attempt: $reconnectAttempts)",
        )

        reconnectJob =
            CoroutineScope(Dispatchers.IO).launch {
                delay(delay)
                currentPrivateKey?.let { key ->
                    connect(key)
                }
            }
    }

    private fun calculateReconnectDelay(): Long {
        reconnectAttempts++
        val exponentialDelay =
            (
                INITIAL_RECONNECT_DELAY_MS *
                    RECONNECT_BACKOFF_MULTIPLIER
                        .toDouble()
                        .pow((reconnectAttempts - 1).toDouble())
            ).toLong()

        val jitter = (exponentialDelay * 0.2 * (Math.random() * 2 - 1)).toLong()

        return min(exponentialDelay + jitter, MAX_RECONNECT_DELAY_MS)
    }

    private suspend fun registerPublicKey(privateKey: PrivateKey): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val publicKeyPEM = privateKey.rsaPublicKey.toPEM()
                val registrationRequestBody =
                    publicKeyPEM.toRequestBody(contentType = "application/x-pem-file".toMediaType())

                val registrationUrl = "$baseUrl/register".toHttpUrl()
                val registrationRequest =
                    Request
                        .Builder()
                        .url(registrationUrl)
                        .post(registrationRequestBody)
                        .build()

                val response = client.newCall(registrationRequest).execute()
                response.isSuccessful || response.code == 304
            } catch (e: Exception) {
                Log.e("MessageWebSocketClient", "Registration failed", e)
                false
            }
        }

    private fun notifyErrorToListeners(error: Throwable) {
        listeners.forEach { it.onError(error) }
    }

    fun addListener(listener: UpdateListener) {
        listeners.add(listener)

        if (webSocket != null) {
            listener.onConnectionStateChange(true)
        }
    }

    fun removeListener(listener: UpdateListener) {
        listeners.remove(listener)
    }

    fun disconnect() {
        isIntentionalDisconnect = true
        reconnectJob?.cancel()
        reconnectJob = null

        webSocket?.close(1000, "Client closed connection")
        webSocket = null

        listeners.forEach { it.onConnectionStateChange(false) }
        listeners.clear()

        currentPrivateKey = null
    }
}
