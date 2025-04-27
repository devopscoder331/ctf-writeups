package whatis.love.agedate.profile.data

import whatis.love.agedate.api.model.Microstatus
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.requests.SetBanRequest

interface ProfileRepository {
    suspend fun getProfileById(
        profileId: String,
        force: Boolean,
    ): Profile

    suspend fun getMicroStatusHistory(
        profileId: String,
        force: Boolean,
    ): List<Microstatus>

    fun saveProfile(profile: Profile)

    suspend fun banProfile(
        profileId: String,
        request: SetBanRequest,
    ): String?
}
