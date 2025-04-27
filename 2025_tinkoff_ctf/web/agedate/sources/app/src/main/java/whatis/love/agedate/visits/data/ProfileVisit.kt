package whatis.love.agedate.visits.data

import kotlinx.serialization.Serializable

@Serializable
data class ProfileVisit(
    val profileID: String,
    val timestamp: Long,
)
