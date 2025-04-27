package whatis.love.agedate.visits.data

import kotlinx.serialization.Serializable

@Serializable
data class ProfileVisitStats(
    val visitCount: Int,
    val lastVisitTimestamp: Long,
)
