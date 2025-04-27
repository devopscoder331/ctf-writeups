package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.Microstatus

@Serializable
data class GetStatusesResponse(
    val statuses: List<Microstatus>,
)
