package whatis.love.agedate.api.requests

import kotlinx.serialization.Serializable

@Serializable
data class RegisterOrLoginRequest(
    val username: String,
    val password: String,
)
