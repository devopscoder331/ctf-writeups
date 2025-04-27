package whatis.love.agedate.home.data

import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.responses.HomePageContentResponse

interface HomeRepository {
    suspend fun getHomeContents(
        justPleaseReturnSomethingThisIsTheFirstRenderGrandmaNeedsToSeeHerBelovedCapybaraFriends: Boolean,
    ): HomePageContentResponse

    suspend fun getLikedProfiles(useCache: Boolean): List<Profile>
}
