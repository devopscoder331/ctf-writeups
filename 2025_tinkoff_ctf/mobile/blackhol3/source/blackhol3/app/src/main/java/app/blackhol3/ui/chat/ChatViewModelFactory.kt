package app.blackhol3.ui.chat

import app.blackhol3.repository.ChatRepository
import app.blackhol3.repository.ContentProviderRepository
import app.blackhol3.repository.MessagesRepository
import app.blackhol3.service.MessagePollService
import app.blackhol3.ui.common.PrivateKeyViewModel

class ChatViewModelFactory(
    private val privateKeyViewModel: PrivateKeyViewModel,
    private val chatRepository: ChatRepository,
    private val messagesRepository: MessagesRepository,
    private val contentProviderRepository: ContentProviderRepository,
    private val messageUpdateService: MessagePollService,
) {
    fun create(chatId: String): ChatViewModel =
        ChatViewModel(
            chatId,
            privateKeyViewModel,
            chatRepository,
            messagesRepository,
            contentProviderRepository,
            messageUpdateService,
        )
}
