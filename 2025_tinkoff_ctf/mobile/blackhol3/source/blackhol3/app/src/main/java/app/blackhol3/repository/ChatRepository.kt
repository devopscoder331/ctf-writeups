package app.blackhol3.repository

import app.blackhol3.data.local.model.Chat
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(privateKey: PrivateKey): Flow<List<Chat>>

    fun refreshChats(privateKey: PrivateKey)

    fun getChatById(
        privateKey: PrivateKey,
        chatId: String,
    ): Chat?

    fun ensureChatByPubKey(
        privateKey: PrivateKey,
        pubKey: PublicKey,
    ): Chat

    fun addChat(
        privateKey: PrivateKey,
        pubKey: PublicKey,
        name: String? = null,
    ): Chat

    fun renameChat(
        privateKey: PrivateKey,
        chatId: String,
        name: String,
    )

    fun deleteChat(
        privateKey: PrivateKey,
        chatId: String,
    )
}
