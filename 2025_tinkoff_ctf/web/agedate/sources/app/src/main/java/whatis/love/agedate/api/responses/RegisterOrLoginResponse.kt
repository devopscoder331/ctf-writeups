package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.MyProfile

@Serializable
data class RegisterOrLoginResponse(
    val authToken: String? = null,
    val userId: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val me: MyProfile? = null,
)
