package whatis.love.agedate.api.requests

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.ProfileLikeStatus

@Serializable
data class SetLikeRequest(
    val status: ProfileLikeStatus,
)
