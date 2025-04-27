package whatis.love.agedate.chats.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.await
import whatis.love.agedate.api.AgeDateAPI
import whatis.love.agedate.api.model.Message
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.requests.SendMessageRequest
import whatis.love.agedate.kv.ActionOnConflict
import whatis.love.agedate.kv.KVStorage
import javax.inject.Inject
import javax.inject.Singleton

private const val KV_TYPE_CHATLIST = "chatlist"
private const val KV_CHATLIST_CHATS = "chats"

@Singleton
class ChatRepositoryImpl
    @Inject
    constructor(
        private val kv: KVStorage,
        private val api: AgeDateAPI,
    ) : ChatRepository {
        companion object {
            private const val STORAGE_TYPE = "chat"
            private const val MESSAGES_KEY_PREFIX = "messages_"
            private const val HAS_MORE_KEY_PREFIX = "has_more_"
            private const val OFFSET_KEY_PREFIX = "offset_"
            private const val CACHE_TTL = 24 * 60 * 60 * 1000L
            private val json =
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    prettyPrint = false
                }
        }

        override suspend fun getChats(): List<Profile>? =
            api
                .chats()
                .await()
                .toResult()
                .map { response ->
                    val chatsList = response.chats
                    kv.setString(
                        KV_TYPE_CHATLIST,
                        KV_CHATLIST_CHATS,
                        Json.encodeToString(chatsList),
                        ActionOnConflict.REPLACE,
                    )
                    chatsList
                }.getOrElse { exception ->
                    kv.getString(KV_TYPE_CHATLIST, KV_CHATLIST_CHATS, 600)?.let { cachedData ->
                        try {
                            Json.decodeFromString<List<Profile>>(cachedData)
                        } catch (e: Exception) {
                            throw exception
                        }
                    } ?: emptyList()
                }

        override suspend fun getMessages(chatId: String): List<Message> = getCachedMessages(chatId)

        override suspend fun loadInitialMessages(chatId: String): Result<List<Message>> {
            return withContext(Dispatchers.IO) {
                try {
                    setCachedOffset(chatId, 0)
                    setCachedHasMore(chatId, true)

                    return@withContext api
                        .messages(chatId)
                        .await()
                        .toResult()
                        .map { messagesResponse ->
                            val messages = messagesResponse.messages
                            val hasMore = messages.size < messagesResponse.total
                            setCachedHasMore(chatId, hasMore)
                            setCachedOffset(chatId, messages.size)
                            cacheMessages(chatId, messages)
                            messages
                        }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

        override suspend fun loadMoreMessages(chatId: String): Result<List<Message>> {
            return withContext(Dispatchers.IO) {
                if (!getCachedHasMore(chatId)) {
                    return@withContext Result.success(emptyList())
                }

                try {
                    val offset = getCachedOffset(chatId)
                    return@withContext api
                        .messages(chatId)
                        .await()
                        .toResult()
                        .map { messagesResponse ->
                            val newMessages = messagesResponse.messages
                            val totalLoaded = offset + newMessages.size
                            val hasMore = totalLoaded < messagesResponse.total
                            setCachedHasMore(chatId, hasMore)
                            setCachedOffset(chatId, totalLoaded)
                            val currentMessages = getCachedMessages(chatId)
                            val combinedMessages =
                                (newMessages + currentMessages)
                                    .distinctBy { it.id }
                                    .sortedBy { it.timestamp }

                            cacheMessages(chatId, combinedMessages)

                            newMessages
                        }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

        override suspend fun sendMessage(
            chatId: String,
            messageText: String,
        ): Result<Message> {
            return withContext(Dispatchers.IO) {
                try {
                    val request = SendMessageRequest(messageText)

                    return@withContext api
                        .sendMessage(chatId, request)
                        .await()
                        .toResult()
                        .map { sendResponse ->
                            val sentMessage = sendResponse.message
                            val currentMessages = getCachedMessages(chatId)
                            val updatedMessages = currentMessages + sentMessage
                            cacheMessages(chatId, updatedMessages)

                            sentMessage
                        }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

        fun clearCache(chatId: String) {
            kv.delete(STORAGE_TYPE, getMessagesKey(chatId))
            kv.delete(STORAGE_TYPE, getHasMoreKey(chatId))
            kv.delete(STORAGE_TYPE, getOffsetKey(chatId))
        }

        private fun getMessagesKey(chatId: String): String = "$MESSAGES_KEY_PREFIX$chatId"

        private fun getHasMoreKey(chatId: String): String = "$HAS_MORE_KEY_PREFIX$chatId"

        private fun getOffsetKey(chatId: String): String = "$OFFSET_KEY_PREFIX$chatId"

        private fun getCachedMessages(chatId: String): List<Message> {
            val cachedData = kv.getString(STORAGE_TYPE, getMessagesKey(chatId), CACHE_TTL)
            return if (cachedData != null) {
                try {
                    json.decodeFromString<List<Message>>(cachedData)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

        private fun cacheMessages(
            chatId: String,
            messages: List<Message>,
        ) {
            val serializedMessages = json.encodeToString(messages)
            kv.setString(
                STORAGE_TYPE,
                getMessagesKey(chatId),
                serializedMessages,
                ActionOnConflict.REPLACE,
            )
        }

        private fun getCachedHasMore(chatId: String): Boolean =
            kv
                .getString(STORAGE_TYPE, getHasMoreKey(chatId), CACHE_TTL)
                ?.toBooleanStrictOrNull() ?: true

        private fun setCachedHasMore(
            chatId: String,
            hasMore: Boolean,
        ) {
            kv.setString(
                STORAGE_TYPE,
                getHasMoreKey(chatId),
                hasMore.toString(),
                ActionOnConflict.REPLACE,
            )
        }

        private fun getCachedOffset(chatId: String): Int = kv.getLong(STORAGE_TYPE, getOffsetKey(chatId), CACHE_TTL)?.toInt() ?: 0

        private fun setCachedOffset(
            chatId: String,
            offset: Int,
        ) {
            kv.setLong(
                STORAGE_TYPE,
                getOffsetKey(chatId),
                offset.toLong(),
                ActionOnConflict.REPLACE,
            )
        }
    }
