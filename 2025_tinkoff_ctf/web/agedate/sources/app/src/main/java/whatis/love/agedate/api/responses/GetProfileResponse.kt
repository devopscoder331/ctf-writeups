package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.AdminVisibleProfileFields
import whatis.love.agedate.api.model.Profile

@Serializable
data class GetProfileResponse(
    val profile: Profile,
    val adminFields: AdminVisibleProfileFields? = null,
)
