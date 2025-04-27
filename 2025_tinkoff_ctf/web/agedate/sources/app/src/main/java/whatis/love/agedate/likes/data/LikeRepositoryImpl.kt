package whatis.love.agedate.likes.data

import kotlinx.serialization.json.Json
import retrofit2.await
import whatis.love.agedate.api.AgeDateAPI
import whatis.love.agedate.api.model.ProfileLikeStatus
import whatis.love.agedate.api.requests.SetLikeRequest
import whatis.love.agedate.kv.ActionOnConflict
import whatis.love.agedate.kv.KVStorage
import whatis.love.agedate.profile.data.KV_TYPE_PROFILE
import javax.inject.Inject

private const val KV_TYPE_PROFILE_LIKE = "profile_likes"

class LikeRepositoryImpl
    @Inject
    constructor(
        val kv: KVStorage,
        val api: AgeDateAPI,
    ) : LikeRepository {
        override suspend fun setLike(
            profileID: String,
            status: ProfileLikeStatus,
        ): Result<Boolean> =
            api.likeProfile(profileID, SetLikeRequest(status)).await().toResult().map {
                kv.setString(KV_TYPE_PROFILE_LIKE, profileID, status.name, ActionOnConflict.REPLACE)
                kv.setString(
                    KV_TYPE_PROFILE,
                    it.profile.id,
                    Json.encodeToString(it.profile),
                    ActionOnConflict.REPLACE,
                )
                it.match
            }

        override suspend fun isLiked(profileID: String): ProfileLikeStatus =
            api
                .profileLiked(profileID)
                .await()
                .toResult()
                .map {
                    kv.setString(KV_TYPE_PROFILE_LIKE, profileID, it.status.name, ActionOnConflict.REPLACE)
                    it.status
                }.getOrElse {
                    kv.getString(KV_TYPE_PROFILE_LIKE, profileID)?.let { ProfileLikeStatus.valueOf(it) }
                } ?: ProfileLikeStatus.NONE
    }
