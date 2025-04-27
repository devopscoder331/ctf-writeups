package whatis.love.agedate.api.client.pinning

import kotlinx.serialization.Serializable

@Serializable
data class PinningParameters(
    val apiRoot: String?,
    val fingerprint: String?,
)
