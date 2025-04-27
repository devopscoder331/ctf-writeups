package whatis.love.agedate.discover.data

import whatis.love.agedate.api.model.Profile

interface DiscoverRepository {
    suspend fun getQueue(evenDisliked: Boolean): List<Profile>
}
