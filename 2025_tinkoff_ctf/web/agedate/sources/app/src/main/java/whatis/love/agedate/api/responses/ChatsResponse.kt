package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.Profile

@Serializable
data class ChatsResponse(
    val chats: List<Profile>,
)
