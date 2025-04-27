package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.Profile

@Serializable
data class SetLikeResponse(
    val profile: Profile,
    val match: Boolean,
)
