package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.ProfileLikeStatus

@Serializable
data class GetProfileLikedStatusResponse(
    val status: ProfileLikeStatus,
    val ts: Long? = null,
)
