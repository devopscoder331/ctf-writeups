package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class SetBanResponse(
    val ok: Boolean = true,
    val error: String? = null,
)
