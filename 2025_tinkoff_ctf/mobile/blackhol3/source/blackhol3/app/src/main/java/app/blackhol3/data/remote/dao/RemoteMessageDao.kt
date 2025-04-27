package app.blackhol3.data.remote.dao

import app.blackhol3.data.local.model.Chat
import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Message
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey

interface RemoteMessageDao {
    suspend fun sendMessage(
        privateKey: PrivateKey,
        chat: Chat,
        message: Message,
    ): DeliveryStatus

    suspend fun getMessages(
        privateKey: PrivateKey,
        chat: Chat,
        limit: Int,
        before: Long,
    ): List<Message>

    suspend fun fetchNewMessages(
        privateKey: PrivateKey,
        since: Long,
    ): List<Pair<PublicKey, Message>>
}
