package whatis.love.agedate.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Announcement(
    val text: String,
    val showTo: List<UserAccessLevel>,
    val priority: Int,
)
