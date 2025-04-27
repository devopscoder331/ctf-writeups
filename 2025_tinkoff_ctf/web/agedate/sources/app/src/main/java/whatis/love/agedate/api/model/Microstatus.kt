package whatis.love.agedate.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Microstatus(
    val text: String,
    val createdAt: Long,
)
