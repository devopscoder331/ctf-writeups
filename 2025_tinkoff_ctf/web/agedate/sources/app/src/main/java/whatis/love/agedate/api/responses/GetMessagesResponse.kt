package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.Message

@Serializable
data class GetMessagesResponse(
    val messages: List<Message>,
    val total: Int,
)
