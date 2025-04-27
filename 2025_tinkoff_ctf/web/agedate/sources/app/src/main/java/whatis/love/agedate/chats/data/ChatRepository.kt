package whatis.love.agedate.chats.data

import whatis.love.agedate.api.model.Message
import whatis.love.agedate.api.model.Profile

interface ChatRepository {
    suspend fun getChats(): List<Profile>?

    suspend fun getMessages(chatId: String): List<Message>

    suspend fun loadInitialMessages(chatId: String): Result<List<Message>>

    suspend fun loadMoreMessages(chatId: String): Result<List<Message>>

    suspend fun sendMessage(
        chatId: String,
        messageText: String,
    ): Result<Message>
}
