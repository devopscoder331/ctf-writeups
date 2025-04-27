package whatis.love.agedate.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MyProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val birthDateVisibility: BirthDateVisibility,
    val bio: String,
    val profilePictureUrl: String?,
    val accessLevel: UserAccessLevel,
    val isOnboarded: Boolean,
    val microstatusHistory: List<Microstatus>,
)
