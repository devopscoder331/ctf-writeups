package whatis.love.agedate.visits.data

interface VisitTrackingRepository {
    fun recordVisit(profileId: String): ProfileVisitStats

    fun profileVisited(profileId: String): Boolean
}
