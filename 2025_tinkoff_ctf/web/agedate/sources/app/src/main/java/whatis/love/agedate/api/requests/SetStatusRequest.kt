package whatis.love.agedate.api.requests

import kotlinx.serialization.Serializable

@Serializable
data class SetStatusRequest(
    val text: String,
)
