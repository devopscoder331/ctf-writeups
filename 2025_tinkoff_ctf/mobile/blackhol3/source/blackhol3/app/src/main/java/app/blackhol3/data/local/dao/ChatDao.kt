package app.blackhol3.data.local.dao

import app.blackhol3.data.local.model.Chat
import app.blackhol3.model.PrivateKey

interface ChatDao {
    fun getChats(privateKey: PrivateKey): List<Chat>

    fun getById(
        privateKey: PrivateKey,
        chatId: String,
    ): Chat?

    fun getByPubKey(
        privateKey: PrivateKey,
        fingerprint: String,
    ): Chat?

    fun addChat(
        privateKey: PrivateKey,
        chat: Chat,
    )

    fun renameChat(
        privateKey: PrivateKey,
        chatId: String,
        name: String,
    )

    fun deleteChat(
        privKeyId: String,
        chatId: String,
    )
}
