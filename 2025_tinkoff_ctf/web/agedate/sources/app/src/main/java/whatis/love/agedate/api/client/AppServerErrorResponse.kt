package whatis.love.agedate.api.client

import kotlinx.serialization.Serializable

@Serializable
data class AppServerErrorResponse(
    val error: String,
    val status: Int,
)
