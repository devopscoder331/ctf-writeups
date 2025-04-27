package whatis.love.agedate.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Long,
    val senderId: String,
    val text: String,
    val timestamp: Long,
)
