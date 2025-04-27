package whatis.love.agedate.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AdminVisibleProfileFields(
    val username: String,
    val registrationTimestamp: Long,
    val lastActivityTimestamp: Long,
    val isBanned: Boolean,
    val banReason: String?,
    val banExpires: Long?,
    val bannedBy: String?,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val birthDateVisibility: BirthDateVisibility,
    val likesSentCount: Int,
)
