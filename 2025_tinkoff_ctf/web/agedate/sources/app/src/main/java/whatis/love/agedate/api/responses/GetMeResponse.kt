package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.MyProfile

@Serializable
data class GetMeResponse(
    val me: MyProfile,
)
