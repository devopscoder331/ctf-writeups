package whatis.love.agedate.api.requests

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.BanAction

@Serializable
data class SetBanRequest(
    val reportRegNo: String,
    val state: BanAction,
    val comment: String,
    val expires: Long?,
)
