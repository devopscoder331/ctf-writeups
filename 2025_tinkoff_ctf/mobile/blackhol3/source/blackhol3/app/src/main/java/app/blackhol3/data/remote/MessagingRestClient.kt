package app.blackhol3.data.remote

import app.blackhol3.data.local.model.Chat
import app.blackhol3.data.remote.envelope.RemoteMessageEnvelope
import app.blackhol3.data.remote.envelope.toRemoteMessageEnvelope
import app.blackhol3.data.remote.model.ApiException
import app.blackhol3.data.remote.model.ServerMessages
import app.blackhol3.data.remote.model.Update
import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Message
import app.blackhol3.model.PrivateKey
import app.blackhol3.service.EncryptionService
import app.blackhol3.util.JWTGenerator
import app.blackhol3.util.fingerprint
import app.blackhol3.util.toPEM
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MessagingRestClient(
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
) {
    companion object {
        private const val JWT_EXPIRATION_SECONDS = 300L
        private val OCTET_STREAM_MEDIA_TYPE = "application/octet-stream".toMediaType()
    }

    private fun createAuthenticatedRequest(
        privateKey: PrivateKey,
        builder: Request.Builder,
    ): Request {
        val token =
            JWTGenerator.generateJWT(
                privateKey = privateKey.rsaPrivateKey,
                keyId = privateKey.rsaPublicKey.fingerprint(),
                issuer = issuer,
                subject = subject,
                expirationTimeSeconds = JWT_EXPIRATION_SECONDS,
            )
        return builder
            .header("Authorization", "Bearer $token")
            .build()
    }

    private suspend fun executeRequest(
        request: Request,
        privateKey: PrivateKey,
    ): Response =
        suspendCoroutine { continuation ->
            client.newCall(request).enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        if (response.code == 401) {
                            try {
                                val originalRequest = request.newBuilder().build()
                                registerAndRetry(originalRequest, privateKey, continuation)
                            } catch (e: Exception) {
                                continuation.resumeWithException(
                                    ApiException(
                                        401,
                                        "Registration failed: ${e.message}",
                                    ),
                                )
                            }
                        } else if (!response.isSuccessful) {
                            val errorBody = response.body?.string()
                            val errorMessage =
                                try {
                                    JSONObject(errorBody ?: "").optString("message", "Unknown error")
                                } catch (e: Exception) {
                                    errorBody ?: "Unknown error"
                                }
                            continuation.resumeWithException(ApiException(response.code, errorMessage))
                        } else {
                            continuation.resume(response)
                        }
                    }
                },
            )
        }

    private fun registerAndRetry(
        originalRequest: Request,
        privateKey: PrivateKey,
        continuation: kotlin.coroutines.Continuation<Response>,
    ) {
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

        client.newCall(registrationRequest).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    continuation.resumeWithException(
                        ApiException(
                            500,
                            "Registration failed: ${e.message}",
                        ),
                    )
                }

                override fun onResponse(
                    call: Call,
                    registrationResponse: Response,
                ) {
                    if (!registrationResponse.isSuccessful && registrationResponse.code != 304) {
                        val errorBody = registrationResponse.body?.string()
                        val errorMessage =
                            try {
                                JSONObject(errorBody ?: "").optString("message", "Unknown error")
                            } catch (e: Exception) {
                                errorBody ?: "Registration failed with code ${registrationResponse.code}"
                            }
                        continuation.resumeWithException(
                            ApiException(
                                registrationResponse.code,
                                errorMessage,
                            ),
                        )
                        return
                    }

                    client.newCall(originalRequest).enqueue(
                        object : Callback {
                            override fun onFailure(
                                retryCall: Call,
                                e: IOException,
                            ) {
                                continuation.resumeWithException(e)
                            }

                            override fun onResponse(
                                retryCall: Call,
                                retryResponse: Response,
                            ) {
                                if (!retryResponse.isSuccessful) {
                                    val errorBody = retryResponse.body?.string()
                                    val errorMessage =
                                        try {
                                            JSONObject(errorBody ?: "").optString("message", "Unknown error")
                                        } catch (e: Exception) {
                                            errorBody ?: "Unknown error"
                                        }
                                    continuation.resumeWithException(
                                        ApiException(
                                            retryResponse.code,
                                            errorMessage,
                                        ),
                                    )
                                } else {
                                    continuation.resume(retryResponse)
                                }
                            }
                        },
                    )
                }
            },
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun parseGetMessagesResponse(
        privateKey: PrivateKey,
        chatId: String,
        responseBody: String,
    ): List<Message> {
        val jsonObject = Json.decodeFromString<ServerMessages>(responseBody)
        val self = privateKey.fingerprint

        return jsonObject.messages.map {
            val message = Base64.decode(it.message)
            val decryptedMessage = encryptionService.decryptString(privateKey, message)
            val envelope =
                Json.decodeFromString<RemoteMessageEnvelope>(decryptedMessage)
            envelope.toMessage(
                chatId = chatId,
                deliveryStatus = if (it.sender != self) DeliveryStatus.INCOMING else DeliveryStatus.DELIVERED,
            )
        }
    }

    suspend fun getMessagesFromSender(
        privateKey: PrivateKey,
        chat: Chat,
        limit: Int = 20,
        since: Long = 0,
    ): List<Message> {
        val url =
            "$baseUrl/from/${chat.pubKey.fingerprint}"
                .toHttpUrl()
                .newBuilder()
                .addQueryParameter("limit", limit.toString())
                .addQueryParameter("since", since.toString())
                .build()

        val request =
            createAuthenticatedRequest(
                privateKey,
                Request.Builder().url(url).get(),
            )

        val response = executeRequest(request, privateKey)
        val responseBody = response.body?.string() ?: throw ApiException(500, "Empty response body")
        return parseGetMessagesResponse(privateKey, chat.id, responseBody)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun parseUpdateResponse(
        privateKey: PrivateKey,
        responseBody: String,
    ): List<Update> {
        val jsonArray = JSONArray(responseBody)
        val updates = mutableListOf<Update>()

        for (i in 0 until jsonArray.length()) {
            try {
                val updateBytes = Base64.decode(jsonArray.getString(i))
                updates.add(
                    Json.decodeFromString(updateBytes.contentToString()),
                )
            } catch (e: Exception) {
            }
        }

        return updates
    }

    suspend fun getUpdatesSince(
        privateKey: PrivateKey,
        since: Long,
    ): List<Update> {
        val url =
            "$baseUrl/updates"
                .toHttpUrl()
                .newBuilder()
                .addQueryParameter("since", since.toString())
                .build()

        val request =
            createAuthenticatedRequest(
                privateKey,
                Request.Builder().url(url).get(),
            )

        val response = executeRequest(request, privateKey)
        val responseBody = response.body?.string() ?: throw ApiException(500, "Empty response body")
        return parseUpdateResponse(privateKey, responseBody)
    }

    suspend fun sendMessage(
        privateKey: PrivateKey,
        chat: Chat,
        message: Message,
    ): DeliveryStatus {
        val url =
            "$baseUrl/send"
                .toHttpUrl()
                .newBuilder()
                .addQueryParameter("fingerprint", chat.pubKey.fingerprint)
                .addQueryParameter("msgId", message.id)
                .build()

        val requestBody =
            message
                .toRemoteMessageEnvelope()
                .encrypt(encryptionService, chat.pubKey)
                .toRequestBody(OCTET_STREAM_MEDIA_TYPE)

        val request =
            createAuthenticatedRequest(
                privateKey,
                Request
                    .Builder()
                    .url(url)
                    .post(requestBody),
            )

        try {
            val response = executeRequest(request, privateKey)
            if (response.code == 201) {
                return DeliveryStatus.DELIVERED
            }
            return DeliveryStatus.FAILED
        } catch (e: Exception) {
            return DeliveryStatus.FAILED
        }
    }
}
