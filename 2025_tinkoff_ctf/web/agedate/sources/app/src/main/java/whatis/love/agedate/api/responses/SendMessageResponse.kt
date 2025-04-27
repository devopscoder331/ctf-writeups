package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.Message

@Serializable
data class SendMessageResponse(
    val message: Message,
)
