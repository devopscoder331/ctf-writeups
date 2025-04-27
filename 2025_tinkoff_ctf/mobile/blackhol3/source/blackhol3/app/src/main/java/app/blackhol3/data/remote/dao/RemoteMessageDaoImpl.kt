package app.blackhol3.data.remote.dao

import app.blackhol3.data.local.model.Chat
import app.blackhol3.data.remote.MessagingRestClient
import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Message
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import app.blackhol3.service.EncryptionService
import app.blackhol3.service.KeyPicGenerationService

class RemoteMessageDaoImpl(
    val api: MessagingRestClient,
    val encryptionService: EncryptionService,
    val keyPicGenerationService: KeyPicGenerationService,
) : RemoteMessageDao {
    override suspend fun sendMessage(
        privateKey: PrivateKey,
        chat: Chat,
        message: Message,
    ): DeliveryStatus = api.sendMessage(privateKey, chat, message)

    override suspend fun getMessages(
        privateKey: PrivateKey,
        chat: Chat,
        limit: Int,
        before: Long,
    ): List<Message> = api.getMessagesFromSender(privateKey, chat, limit, before)

    override suspend fun fetchNewMessages(
        privateKey: PrivateKey,
        since: Long,
    ): List<Pair<PublicKey, Message>> {
        val result = api.getUpdatesSince(privateKey, since)
        return result.mapNotNull { u ->
            try {
                u.pubKey(keyPicGenerationService) to
                    u
                        .envelope(encryptionService, privateKey)
                        .toMessage(chatId = "")
            } catch (e: Exception) {
                null
            }
        }
    }
}
