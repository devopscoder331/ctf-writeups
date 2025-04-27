package whatis.love.agedate.visits.data

import kotlinx.serialization.json.Json
import whatis.love.agedate.kv.ActionOnConflict
import whatis.love.agedate.kv.KVStorage
import whatis.love.agedate.user.data.KV_TYPE_USER
import javax.inject.Inject

private const val KV_TYPE_PROFILE_VISIT = "profile_visits"
private const val KV_TYPE_PROFILE_VISITED = "profile_visited"
private const val KV_KEY_USER_HISTORY = "history"

class VisitTrackingRepositoryImpl
    @Inject
    constructor(
        val kv: KVStorage,
    ) : VisitTrackingRepository {
        override fun recordVisit(profileId: String): ProfileVisitStats {
            val now = System.currentTimeMillis()
            kv.setString(KV_TYPE_PROFILE_VISITED, profileId, "true", ActionOnConflict.REPLACE)
            val visited: MutableList<ProfileVisit> =
                kv.getString(KV_TYPE_USER, KV_KEY_USER_HISTORY, 600)?.let {
                    Json.Default.decodeFromString(it)
                } ?: mutableListOf()
            if (visited.none { it.profileID == profileId }) {
                visited.add(
                    ProfileVisit(
                        profileID = profileId,
                        timestamp = now,
                    ),
                )
            }
            if (visited.size > 10) {
                visited.removeAt(0)
            }

            kv.setString(
                KV_TYPE_USER,
                KV_KEY_USER_HISTORY,
                Json.Default.encodeToString(visited),
                ActionOnConflict.REPLACE,
            )
            val previousVisits = kv.getString(KV_TYPE_PROFILE_VISIT, profileId)?.toIntOrNull() ?: 0
            kv.setString(
                KV_TYPE_PROFILE_VISIT,
                profileId,
                (previousVisits + 1).toString(),
                ActionOnConflict.REPLACE,
            )
            val previousLastVisit =
                kv.getString(KV_TYPE_PROFILE_VISITED, profileId)?.toLongOrNull() ?: 0

            return ProfileVisitStats(
                visitCount = previousVisits + 1,
                lastVisitTimestamp = previousLastVisit,
            )
        }

        override fun profileVisited(profileId: String): Boolean = kv.getString(KV_TYPE_PROFILE_VISITED, profileId)?.toBoolean() == true
    }
