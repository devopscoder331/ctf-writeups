package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.MyProfile

@Serializable
data class SetStatusResponse(
    val profile: MyProfile?,
    val error: String?,
)
