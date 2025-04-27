package app.blackhol3.repository

import app.blackhol3.data.local.dao.ChatDao
import app.blackhol3.data.local.model.Chat
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class ChatRepositoryImpl(
    val dao: ChatDao,
) : ChatRepository {
    private val updates = MutableSharedFlow<Pair<String, Chat?>>()
    private val chats = mutableMapOf<String, MutableStateFlow<List<Chat>>>()

    override fun getChats(privateKey: PrivateKey): Flow<List<Chat>> {
        refreshChats(privateKey)
        return chats[privateKey.id]!!
    }

    override fun refreshChats(privateKey: PrivateKey) {
        if (chats[privateKey.id] == null) {
            chats[privateKey.id] = MutableStateFlow(dao.getChats(privateKey))
        } else {
            chats[privateKey.id]?.value = dao.getChats(privateKey)
        }
    }

    private fun refreshChat(
        privateKey: PrivateKey,
        chatId: String,
        chat: Chat?,
    ) {
        updates.tryEmit(Pair(chatId, chat))
        refreshChats(privateKey)
    }

    override fun getChatById(
        privateKey: PrivateKey,
        chatId: String,
    ): Chat? = dao.getById(privateKey, chatId)

    override fun ensureChatByPubKey(
        privateKey: PrivateKey,
        pubKey: PublicKey,
    ): Chat {
        val existing = dao.getByPubKey(privateKey, pubKey.fingerprint)
        if (existing != null) {
            return existing
        }
        return addChat(privateKey, pubKey)
    }

    override fun addChat(
        privateKey: PrivateKey,
        pubKey: PublicKey,
        name: String?,
    ): Chat {
        val newChat =
            Chat(
                id = UUID.randomUUID().toString(),
                privKeyId = privateKey.id,
                name = name ?: pubKey.fingerprint,
                pubKey = pubKey,
            )
        dao.addChat(privateKey, newChat)
        refreshChats(privateKey)
        return newChat
    }

    override fun renameChat(
        privateKey: PrivateKey,
        chatId: String,
        name: String,
    ) {
        dao.renameChat(privateKey, chatId, name)
        refreshChat(privateKey, chatId, getChatById(privateKey, chatId))
    }

    override fun deleteChat(
        privateKey: PrivateKey,
        chatId: String,
    ) {
        dao.deleteChat(privateKey.id, chatId)
        refreshChat(privateKey, chatId, null)
    }
}
