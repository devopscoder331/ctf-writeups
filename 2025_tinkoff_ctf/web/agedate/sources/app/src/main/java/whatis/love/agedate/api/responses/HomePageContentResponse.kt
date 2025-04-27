package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.Announcement
import whatis.love.agedate.api.model.Profile

@Serializable
data class HomePageContentResponse(
    val featuredProfile: Profile,
    val randomProfiles: List<Profile>,
    val announcements: List<Announcement>,
)
