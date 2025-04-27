package whatis.love.agedate.api.client

import kotlinx.serialization.Serializable

@Serializable
data class APIErrorResponse(
    val error: String,
    val status: Int? = null,
)
