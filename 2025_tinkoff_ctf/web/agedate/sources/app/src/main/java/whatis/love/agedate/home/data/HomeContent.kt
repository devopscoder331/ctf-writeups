package whatis.love.agedate.home.data

import whatis.love.agedate.api.model.Announcement
import whatis.love.agedate.api.model.Profile

data class HomeContent(
    val featuredProfile: Profile,
    val randomProfiles: List<Profile>,
    val announcements: List<Announcement> = emptyList(),
)
