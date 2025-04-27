package whatis.love.agedate.api.requests

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val message: String,
)
