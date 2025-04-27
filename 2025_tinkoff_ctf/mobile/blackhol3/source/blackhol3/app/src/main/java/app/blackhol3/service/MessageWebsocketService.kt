package app.blackhol3.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import app.blackhol3.data.remote.MessageWebSocketClient
import app.blackhol3.data.remote.model.Update
import app.blackhol3.model.PrivateKey
import app.blackhol3.repository.MessagesRepository
import app.blackhol3.repository.PrivateKeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class MessageWebSocketService(
    private val client: OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .build(),
    private val encryptionService: EncryptionService,
    private val baseUrl: String,
    private val issuer: String,
    private val subject: String,
    private val messagesRepository: MessagesRepository,
    private val privateKeyRepository: PrivateKeyRepository,
    private val keyPicGenerationService: KeyPicGenerationService,
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocketClient: MessageWebSocketClient? = null
    private var currentKey: PrivateKey? = null
    private val updateListener =
        object : MessageWebSocketClient.UpdateListener {
            override fun onUpdate(update: Update) {
                CoroutineScope(Dispatchers.IO).launch {
                    currentKey?.let {
                        messagesRepository.incomingMessage(
                            privateKey = it,
                            senderPublicKey = update.pubKey(keyPicGenerationService),
                            message = update.envelope(encryptionService, it).toMessage(chatId = ""),
                        )
                    }
                }
            }

            override fun onError(error: Throwable) {
                Log.e("MessageWebSocketService", "WebSocket error", error)
            }

            override fun onConnectionStateChange(connected: Boolean) {
                Log.d("MessageWebSocketService", "Connection state changed: $connected")
            }
        }

    init {
        CoroutineScope(Dispatchers.Main).launch {
            privateKeyRepository.currentPrivateKey().collect { privateKey ->
                handlePrivateKeyChange(privateKey)
            }
        }
    }

    private suspend fun handlePrivateKeyChange(privateKey: PrivateKey?) {
        if (privateKey == null || privateKey.id != currentKey?.id) {
            disconnectCurrent()
        }

        if (privateKey != null) {
            currentKey = privateKey
            connect(privateKey)
        }
    }

    private suspend fun connect(privateKey: PrivateKey) {
        try {
            if (webSocketClient == null) {
                webSocketClient =
                    MessageWebSocketClient(
                        client = client,
                        baseUrl = baseUrl,
                        issuer = issuer,
                        subject = subject,
                    )
                webSocketClient?.addListener(updateListener)
            }
            webSocketClient?.connect(privateKey)

            Log.d("MessageWebSocketService", "Connected WebSocket for key: ${privateKey.id}")
        } catch (e: Exception) {
            Log.e("MessageWebSocketService", "Failed to connect WebSocket", e)
        }
    }

    private fun disconnectCurrent() {
        webSocketClient?.let {
            it.removeListener(updateListener)
            it.disconnect()
            Log.d("MessageWebSocketService", "Disconnected previous WebSocket")
        }
        webSocketClient = null
        currentKey = null
    }

    fun shutdown() {
        disconnectCurrent()
        serviceScope.cancel()
    }
}

class MessageWebSocketBackgroundService : Service() {
    private val messageWebSocketService: MessageWebSocketService by inject()

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        messageWebSocketService.toString()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        messageWebSocketService.shutdown()
    }
}
